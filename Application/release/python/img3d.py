import json
import requests
import base64
import time
import sys

def process(image_path):
    def convert_jpg_to_data_uri(image_path):
        """
        Converts a JPG image to a Base64-encoded data URI.
        """
        try:
            with open(image_path, "rb") as image_file:
                # Read the binary data of the image
                image_data = image_file.read()
                # Encode the binary data to Base64
                base64_encoded = base64.b64encode(image_data).decode("utf-8")
            # Add the MIME type prefix for a JPEG image
            data_uri = f"data:image/jpg;base64,{base64_encoded}"
            return data_uri
        except FileNotFoundError:
            print(f"Error: The file at '{image_path}' was not found.")
            return None
        except Exception as e:
            print(f"An error occurred: {e}")
            return None
        

    def makeApiCall(data_uri):
    
        if not data_uri:
            print("Error: Data URI is None. Cannot proceed with API call.")
            return

        # Prepare payload and headers
        payload = {
            "image_url": data_uri,
            "enable_pbr": True,  # Physically Based Rendering (if required)
            "enable_texture": True,  # Enable texture (new field)
            "enable_color": True,  # Enable color (new field, if supported)
        }
        headers = {
            "Authorization": "Bearer msy_3qaBPcFOKW1v2XbQKox9Vvq2a7vsPWeY9TzR"
        }

        try:
            # Make POST request
            response = requests.post(
                "https://api.meshy.ai/v1/image-to-3d",
                headers=headers,
                json=payload,
            )
            response.raise_for_status()  # Raise exception for HTTP errors
            data = response.json()  # Parse JSON response

            # Extract task ID
            task_id = data.get("result")
            if not task_id:
                print("Error: Task ID not found in response.")
                return

            # Wait before making the GET request
            time.sleep(5)  # Wait for the task to complete

            # Fetch 3D model details
            get_model(task_id, headers)
        except requests.exceptions.RequestException as e:
            print(f"An error occurred during the API call: {e}")
        except json.JSONDecodeError:
            print("Error decoding JSON response from the API.")
    def get_model(task_id, headers, retries=100, delay=10):

        for attempt in range(retries):
            try:
                # Make GET request to retrieve task details
                response = requests.get(
                    f"https://api.meshy.ai/v1/image-to-3d/{task_id}",
                    headers=headers,
                )
                response.raise_for_status()

                # Parse the JSON response
                task_data = response.json()

                # Check task status
                status = task_data.get("status")
                if status == "SUCCEEDED":
                    # Check for the 3D model URL
                    model_url = task_data.get("model_urls", {}).get("glb")
                    if model_url:
                        download_model(model_url, task_id, "glb")
                        return  # Exit the loop and function once the file is downloaded
                    else:
                        print("Model URL for 'glb' format not found in the response.")
                        return
                elif status == "FAILED":
                    print("The task has failed. Please check the API logs for details.")
                    return
                else:
                    time.sleep(delay)
            except requests.exceptions.RequestException as e:
                print(f"An error occurred while retrieving the model: {e}")
                return


    def download_model(model_url, task_id, format):
        """
        Downloads the 3D model from the provided URL and saves it locally.
        """
        try:
            # Download the 3D model
            model_response = requests.get(model_url, stream=True)
            model_response.raise_for_status()

            # Save the 3D model locally
            output_file = f"out\model\{task_id}.{format}"
            with open(output_file, "wb") as file:
                for chunk in model_response.iter_content(chunk_size=8192):
                    file.write(chunk)

        except requests.exceptions.RequestException as e:
            print(f"An error occurred while downloading the model: {e}")

    # Convert image to data URI
    data_uri = convert_jpg_to_data_uri(image_path)
    if data_uri:
        makeApiCall(data_uri)

    return "model saved"


# Run the process with the specified image
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
            output = process(input_data.strip())
        except Exception as e:
            raise RuntimeError(f"[nlp]Error in process function: {e}")
        
        print(json.dumps(output))      #java app reads from stdout
    
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
