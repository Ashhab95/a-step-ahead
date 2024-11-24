import sys
import json
import re

field_values = {
    "Category": ['Boots', 'Shoes', 'Sandals', 'Slippers'],
    "SubCategory": ['Crib Shoes', 'Sneakers', 'Slipper Heels', 
                    'Firstwalker', 'Oxfords', 'Athletic', 'Ankle', 'Flats', 'Loafers', 
                    'Heel', 'Flat', 'Over the Knee', 'Boot', 'Prewalker', 'Mid-Calf'],
    "HeelHeight": {
        '1in - 1 3/4in': [1, 1.75],
        '2in - 2 3/4in': [2, 2.75],
        '3in - 3 3/4in': [3, 3.75],
        '4in - 4 3/4in': [4, 4.75],
        '5in & over': [5, float('inf')],
        'Flat': [0, 0],
        'Under 1in': [0, 1]
    },
    "Gender": ['Women', 'Men', 'Girls', 'Boys;Girls', 'Boys', 'Men;Women'],
    "Material": ['Full-grain', 'Leather', 'Nylon', 'Suede', 'Cork', 'Mesh', 'Rubber',
                 'Nubuck', 'Synthetic', 'Microfiber', 'Polyester', 
                 'Patent Leather', 'Wool', 'Fur', 'EVA', 'Vinyl', 'Faux',
                 'Linen', 'Terry'],
    "Colours": ['red', 'blue', 'green', 'yellow', 'orange', 'purple', 'pink', 
              'brown', 'cyan', 'magenta', 'black', 'white']
}


def detect_field_selection(text, field_values):
    results = [0] * len(field_values)  # Initialize results

    for idx, (field, values) in enumerate(field_values.items()):
        if field == "HeelHeight":
            # Process all matches for HeelHeight and keep the latest one
            heights = list(re.finditer(r'(\d+(\.\d+)?)(\s*in|\s*inches)', text, re.IGNORECASE))
            if heights:
                latest_height = float(heights[-1].group(1))  # Take the last match
                results[idx] = find_closest_heel_height(latest_height, field_values["HeelHeight"])
        else:
            # Collect all matches for the field
            field_matches = []
            for value_idx, value in enumerate(values, start=1):
                matches = list(re.finditer(rf'\b{re.escape(value)}\b', text, re.IGNORECASE))
                for match in matches:
                    match_position = match.start()
                    context = extract_context(text, match_position)
                    sentiment = analyze_sentiment(value, context)
                    field_matches.append((match_position, value_idx, sentiment))
            # After collecting all matches, pick the one that occurs last in the text
            if field_matches:
                # Find the match with the highest match_position (latest in text)
                latest_match = max(field_matches, key=lambda x: x[0])
                _, value_idx, sentiment = latest_match
                if sentiment == "Positive":
                    results[idx] = value_idx
                elif sentiment == "Negative":
                    results[idx] = -value_idx

    return results


def extract_context(text, match_start, window=4):
    """
    Extracts a context of up to `window` words before the matched value.
    """
    words = text[:match_start].split()
    context_words = words[-window:]
    return " ".join(context_words)


def analyze_sentiment(value, context):
    negative_words = [
        "not", "don't", "without", "no", "excluding", "never", "none", 
        "neither", "except", "avoid", "prohibit", "deny", "reject"
    ]
    words = context.lower().split()
    if any(neg_word in words for neg_word in negative_words):
        return "Negative"
    return "Positive"

def find_closest_heel_height(height, heel_ranges):
    for idx, (key, range_) in enumerate(heel_ranges.items(), start=1):
        if range_[0] <= height <= range_[1]:
            return idx
    return 0

def map_single_material_to_number(material):
    material_categories = {
        1: ['Full-grain', 'Leather', 'Nylon', 'Suede'],
        2: ['Cork', 'Leather', 'Mesh', 'Rubber'],
        3: ['Mesh', 'Nubuck', 'Rubber', 'Synthetic'],
        4: ['Microfiber', 'Mesh', 'Nylon', 'Polyester'],
        5: ['Leather', 'Patent Leather', 'Suede'],
        6: ['Suede', 'Wool'],
        7: ['Leather', 'Nubuck', 'Faux Fur'],
        8: ['Suede', 'EVA'],
        9: ['Polyester', 'Rubber', 'Vinyl'],
        10: ['Microfiber', 'Nylon'],
        11: ['Faux Leather', 'Mesh', 'Nylon'],
        12: ['Polyester', 'Rubber'],
        13: ['Linen', 'Leather', 'Rubber', 'Suede'],
        14: ['Leather', 'Terry'],
        15: ['Microfiber']
    }
    material_to_number = {}
    for number, materials in material_categories.items():
        for mat in materials:
            if mat not in material_to_number:
                material_to_number[mat] = number

    return material_to_number.get(material, 0)

def transform_list(int_list):
    transformed_list = []
    for value in int_list:
        if value == 0:
            transformed_list.append(1)
        elif value < 0:
            if value == -1:
                transformed_list.append(2)
            transformed_list.append(abs(value) - 1)
        else:
            transformed_list.append(value)
    return transformed_list
def reverse_words(input_string):
    """
    Takes a string, separates it into words using space as a delimiter,
    reverses the list of words, and concatenates them back into a string
    with spaces in between.

    Args:
        input_string (str): The input string.

    Returns:
        str: The reversed string.
    """
    # Split the string into a list of words
    words = input_string.split()
    # Reverse the list of words
    reversed_words = words[::-1]
    # Join the reversed words back into a single string
    reversed_string = " ".join(reversed_words)
    return reversed_string

def process(inputString):
    input_materials = ['Full-grain', 'Leather', 'Nylon', 'Suede', 'Cork', 'Mesh', 'Rubber', 
                       'Nubuck', 'Synthetic', 'Microfiber', 'Polyester', 
                       'Patent Leather', 'Wool', 'Fur', 'EVA', 'Vinyl', 'Faux',
                       'Linen', 'Terry']

    #inputString = reverse_words(inputString)
   
    

    input_text_1 = inputString


    parsed_results_1 = detect_field_selection(input_text_1, field_values)



    if parsed_results_1[4] > 0:  # Check for a valid material index
        output_number = map_single_material_to_number(input_materials[parsed_results_1[4] - 1])
        parsed_results_1[4] = output_number


    finalList = transform_list(parsed_results_1[:-1])

    colourList = ['none', 'red', 'blue', 'green', 'yellow', 'orange', 'purple', 'pink', 
                  'brown', 'cyan', 'magenta', 'black', 'white']

    finalList.append(colourList[parsed_results_1[-1]])

    return finalList




# process( input ) to return the final result you want



############
# Recieves a string input from java application and 
# returns a list of numbers with parameters of model 
###########

def main():
    try:
        input_data = sys.stdin.read()       # Read input from stdin
        
        if not input_data.strip():
            raise ValueError("[nlp]input not recieved")
        
        try:
            output_list = process(input_data.strip())
        except Exception as e:
            raise RuntimeError(f"[nlp]Error in process function: {e}")
        
        print(json.dumps(output_list))      #java app reads from stdout
    
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