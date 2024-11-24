import sys
import json
import argparse
import pandas as pd
import random
from sklearn.preprocessing import OneHotEncoder
from sklearn.impute import SimpleImputer
from sklearn.ensemble import RandomForestRegressor
import numpy as np
import os
import requests
from bs4 import BeautifulSoup
from PIL import Image
import ast

def parse_string_to_list(input_string):
    """
    Converts a string of format '[1, 1, 1, 1, 14, "some_string"]' into an actual Python list.
    
    Args:
        input_string (str): The input string to parse.
    
    Returns:
        list: The parsed Python list.
    """
    try:
        # Use `ast.literal_eval` for safe evaluation of the string
        parsed_list = ast.literal_eval(input_string.strip())
        
        # Ensure the parsed object is a list
        if not isinstance(parsed_list, list):
            raise ValueError("The input string does not represent a list.")
        
        return parsed_list
    except Exception as e:
        print(f"Error parsing input string: {e}")
        return None



# Constants
HEADERS = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36",
    "Accept-Language": "en-US,en;q=0.9",
    "Accept-Encoding": "gzip, deflate, br",
}
IMAGE_DIR = "out\img"
os.makedirs(IMAGE_DIR, exist_ok=True)

# Load the dataset
file_path = r"resources\csv\mini_dataset.csv"
df = pd.read_csv(file_path)

# Retain CID for reference, but exclude it from processing
cid_column = "CID"
columns_to_exclude = ["Insole", "Closure", "ToeStyle"]
df_features = df.drop(columns=columns_to_exclude)

# Handle missing values
imputer = SimpleImputer(strategy="most_frequent")
df_without_cid = df_features.drop(columns=[cid_column])  # Exclude CID for preprocessing
df_filled = pd.DataFrame(imputer.fit_transform(df_without_cid), columns=df_without_cid.columns)

# Add CID back to the filled dataset
df_filled[cid_column] = df[cid_column]

# Encode categorical variables
categorical_columns = ["Category", "SubCategory", "HeelHeight", "Gender", "Material"]
encoder = OneHotEncoder(sparse_output=False, handle_unknown="ignore")
encoded_features = encoder.fit_transform(df_filled[categorical_columns])
encoded_df = pd.DataFrame(encoded_features, columns=encoder.get_feature_names_out(categorical_columns))

# Prepare unique options for user input
unique_values = {col: df_filled[col].dropna().unique().tolist() for col in categorical_columns}

# Parse command-line arguments
def parse_arguments():
    parser = argparse.ArgumentParser(description="Shoe Recommendation System")
    parser.add_argument(
        "choices",
        metavar="choices",
        type=int,
        nargs=5,
        help="A list of 5 integers representing user choices for each field.",
    )
    args = parser.parse_args()
    return args.choices

# User input
def process_user_input(choices):
    if len(choices) != 5:
        raise ValueError("You must provide exactly 5 choices.")
    
    user_input = {}
    for idx, field in enumerate(categorical_columns):
        options = unique_values[field]
        if choices[idx] > 0 and choices[idx] <= len(options):
            user_input[field] = options[choices[idx] - 1]
        else:
            user_input[field] = None
    return user_input

# Recommend shoe
def recommend_shoe(user_input):
    filtered_df = df_filled.copy()

    # Filter for exact matches
    for key, value in user_input.items():
        if value is not None:
            filtered_df = filtered_df[filtered_df[key] == value]

    if filtered_df.empty:
        # Relax filtering criteria: match based on most important fields
        filtered_df = df_filled.copy()
        relaxed_fields = ["Category", "SubCategory", "Material", "Gender"]
        for key in relaxed_fields:
            if user_input.get(key):
                filtered_df = filtered_df[filtered_df[key] == user_input[key]]

    if filtered_df.empty:
        return df.sample(1).iloc[0].to_dict()

    # Train the model with the filtered dataset
    y = filtered_df.index  # Use index for consistent recommendation
    model = RandomForestRegressor(n_estimators=100, random_state=42)
    model.fit(encoded_df.loc[filtered_df.index], y)

    # Encode user input and predict scores
    user_df = pd.DataFrame([user_input])
    user_filled = pd.DataFrame(imputer.transform(user_df), columns=user_df.columns)
    user_encoded = pd.DataFrame(
        encoder.transform(user_filled),
        columns=encoder.get_feature_names_out(categorical_columns),
    )
    predicted_index = int(model.predict(user_encoded)[0])

    return df.loc[predicted_index].to_dict()

# Fetch image
def fetch_image(cid):
    formatted_cid = cid.replace(".", "_")
    image_filename = f"{cid}.jpg"
    destination_image_path = os.path.join(IMAGE_DIR, f"{formatted_cid}.jpg")

    try:
        for root, dirs, files in os.walk(r"resources\ut-zap50k-images"):
            if image_filename in files:
                source_image_path = os.path.join(root, image_filename)
                with open(source_image_path, "rb") as source_file:
                    with open(destination_image_path, "wb") as destination_file:
                        destination_file.write(source_file.read())
                return formatted_cid
        return fetch_similar_image(cid)
    except:
        return None

def fetch_similar_image(cid):
    category_dir = cid.split("_")[0]
    for root, dirs, files in os.walk(r"resources\ut-zap50k-images"):
        for file in files:
            if category_dir in file:
                similar_image_path = os.path.join(root, file)
                destination_image_path = os.path.join(IMAGE_DIR, file)
                with open(similar_image_path, "rb") as source_file:
                    with open(destination_image_path, "wb") as destination_file:
                        destination_file.write(source_file.read())
                return file
    return None

def construct_search_keywords(parsed_attributes):
    relevant_keys = ["Gender", "Category", "SubCategory", "Material"]
    keywords = [
        str(parsed_attributes.get(key, "")).strip() if pd.notna(parsed_attributes.get(key)) else ""
        for key in relevant_keys
    ]
    return " ".join(keywords).strip()

def fetch_amazon_results(keywords):
    query = "+".join(keywords.split()) + "+shoes"
    search_url = f"https://www.amazon.com/s?k={query}"
    links = []

    try:
        response = requests.get(search_url, headers=HEADERS)
        if response.status_code == 200:
            soup = BeautifulSoup(response.text, "html.parser")
            results = soup.find_all("div", {"data-component-type": "s-search-result"})

            for result in results[:10]:
                if result.h2 and result.h2.a and "href" in result.h2.a.attrs:
                    title = result.h2.text.strip().lower()
                    if any(keyword in title for keyword in ["shoe", "sneaker", "boot", "sandal", "slipper"]):
                        link = "https://www.amazon.com" + result.h2.a["href"]
                        links.append(link)

                if len(links) == 3:
                    break
    except:
        pass

    return links



def process(choices):
    try:
        user_input = process_user_input(choices)
        recommendation = recommend_shoe(user_input)
        cid = recommendation.get("CID", "").replace("-", ".")
        image_name = fetch_image(cid) if cid else None
        search_keywords = construct_search_keywords(recommendation)
        amazon_links = fetch_amazon_results(search_keywords)
        output = [image_name] + amazon_links
        return output
    except:
        pass


#main_ouput = process([1,1,1,1,1])



##################################################################################
##################################################################################
###############################MAIN_LOGIC_END#####################################
##################################################################################
##################################################################################



############
# Recieves a string input from java application and 
# returns a list of numbers with parameters of model 
###########

def main():
    try:
        input_data = sys.stdin.read()       # Read input from stdin
    
        if not input_data.strip():
            raise ValueError("No input data provided.")
        
        # Print the received input for debugging purposes to stderr
        #print(f"Received input from java: {input_data}", file=sys.stderr)
        
        # Try to parse the input as JSON
        try:
            input_list = json.loads(input_data)
        except json.JSONDecodeError:
            print("not a valid input[model]")
            raise ValueError("Input is not valid JSON.")
        

        if not isinstance(input_list, list):
            print(f"NOt a list: {input_list}", file=sys.stderr)
            raise TypeError("Input is not a list.")
        
        try:
            input_data = input_data.strip()
            listn = parse_string_to_list(input_data)
            listn=listn[0:5]
           
            output_list = process(listn)

            print(json.dumps(output_list)) 
        except Exception as e:
            raise RuntimeError(f"[nlp]Error in process function: {e}")
        
             #java app reads from stdout
    
    except ValueError as e:
        print(f"[nlp]Error: {e}", file=sys.stderr)
        sys.exit(1)
    
    except RuntimeError as e:
        print(f"[nlp]Error: {e}", file=sys.stderr)
        sys.exit(3)
    
    except Exception as e:
        print(f"[nlp] Unexpected error: {e}", file=sys.stderr)
        sys.exit(4)

if __name__ == "__main__":
    main()
