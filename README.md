
# A Step Ahead - Application

## Introduction

**A Step Ahead** is a personalized shoe recommendation application designed to simplify shoe shopping for everyone, everywhere. The project aims to assist individuals, including those with disabilities and limited internet access, by providing tailored shoe suggestions based on user preferences and needs.

## Features

- **Personalized Recommendations**: Utilizes user input to suggest shoes that match individual preferences.
- **Accessibility Focused**: Designed with features to aid disabled individuals in making informed choices.
- **Offline Functionality**: Offers capabilities for users with limited or no internet access.
- **User-Friendly Interface**: Intuitive design ensuring ease of use for all age groups.

## Technologies Used

- **Programming Languages**: Python, Java
- **Frameworks**: Flask
- **Libraries**: Pandas, NumPy, Scikit-learn
- **Tools**: Git

## How to Run the Application

Follow these steps to run the Java-based **A Step Ahead** application:

### Prerequisites

- **Java Development Kit (JDK)**: Ensure that JDK 8 or higher is installed on your system. You can download it from the [Oracle website](https://www.oracle.com/java/technologies/downloads/).
- **Python**: Ensure that Python 3.x is installed. You can download it from the [official website](https://www.python.org/downloads/).

### Installation

1. **Clone the Repository**

   Open your terminal and run the following command to clone the repository:

   ```bash
   git clone https://github.com/Ashhab95/a-step-ahead.git
   ```

2. **Navigate to the Application Directory**

   ```bash
   cd a-step-ahead/Application
   ```

3. **Install Python Dependencies**

   ```bash
   pip install -r requirements.txt
   ```

### Usage

1. **Compile the Java Files**

   ```bash
   javac -d bin src/*.java
   ```

   This will compile the Java files in the `src` directory and place the `.class` files in the `bin` directory.

2. **Run the Main Class**

   Assuming the main class is named `Main`, run the following command:

   ```bash
   java -cp bin Main
   ```

   If your main class is part of a package, include the package name, for example:

   ```bash
   java -cp bin com.example.Main
   ```

   *(Replace `com.example.Main` with the actual package and main class name.)*

3. **Access the Application**

   Open your web browser and navigate to `http://localhost:5000` to use the application.

## License

This project is licensed under the [MIT License](LICENSE).

## Contact

For any inquiries or feedback, please contact Kazi Ashhab Rahman at [kazi.a.rahman@mail.mcgill.ca](mailto:kazi.a.rahman@mail.mcgill.ca).

Devpost Link for additional information: https://devpost.com/software/shoe-bot#updates

