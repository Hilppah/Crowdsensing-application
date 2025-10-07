# Crowdsensing-aplication
This is a project done for OAMK.

A mobile app made using Kotlin for the app, and JavaScript (Node.js) for the backend. For the database, I used MongoDB Atlas, and the backend is deployed on Render.

In the application, users can collect sensor data from their phone (GPS, accelerometer, gyroscope, compass, proximity) and find Wi-Fi and bluetooth networks, add optional comments, and send the data to the backend. The sessions are stored in MongoDB Atlas. The app also includes a view where stored sessions can be searched and inspected in detail.

## Installation

### Run frontend
* Clone the project
* Open the "app" folder in Android Studio
* Run on your own device or an emulator

## Run backend locally
The backend won't work on your phone if you choose to run it locally
* Clone the project
* Install dependencies
  ```
    cd backend
    npm install
  ```
* Create a .env file in backend/ with:
  ```
  MONGO_URI=your-mongodb-atlas-uri
  PORT=3000
  ```
* Start server
  ```
  node server.js
  ```
  
## Database
  * The app uses MongoDB Atlas (cloud hosted MongoDB)
  * Add your URI to .env
      * example:
        
        ```
           MONGO_URI=mongodb+srv://<user>:<password>@cluster.yourid.mongodb.net/Crowdsensing 
        ```
  * Replace <user> and <password> with your MongoDB Atlas credentials
  * Collections initialize automatically when the backend starts
 
## Deployment
  * Go to render.com and create a new project
  * Choose the "Web Service" option
  * Choose this repository
  * Add the build command
      ```
      npm --prefix backend install
      ``` 
  * Add the start command
     ```
     npm --prefix backend start
     ```
  * Add the environment variables from your .env
       * Value is where URI goes
         ```
         MONGO_URI=mongodb+srv://<user>:<password>@cluster.yourid.mongodb.net/Crowdsensing 
         ```
    * Then deploy web service 

  ## Pictures and demo video
  Will be added soon




-------------------------------
#### Thank you :)
  
  
