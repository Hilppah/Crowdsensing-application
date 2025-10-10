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
 * [Demo video of the project](https://youtu.be/cqH5xqsNZjo)

* Pictures

![homeFragment](https://github.com/user-attachments/assets/b1e78992-6c3f-4ae9-9855-b0a390808a0d)

 ### Home page where you can start saving data.

![newDataFragment](https://github.com/user-attachments/assets/1cd0445c-6731-4a97-80fb-6355f07b3bf4)
 
### A page where you can see how many points you got from the session recording. There you can also add a comment and save the data.

![viewDataFragment-Juomapullo](https://github.com/user-attachments/assets/9bef119d-0167-448f-aea3-9a2b3bd53a33)

### A page where the summarys of sessions are displayed. At the top you can see the session that we saved in the image above. 

![sessionPopUp](https://github.com/user-attachments/assets/e9b052e1-d6a2-4e15-9d94-5cc6dd766852)

### When you click on a session you can see the sensor data that was saved.

![viewDataFragment-Search](https://github.com/user-attachments/assets/077f2ef4-2764-462e-8789-afc7385d1930)

### You can search for what type of data you want to find by typing the phone model or what the user was measuring.

![viewDataFragment-Arrange](https://github.com/user-attachments/assets/f929b071-1be9-46c6-a92f-c18f0d57d6e5)

### You can also arrange the data by filtering to see the oldest or the newest sessions first.

![delete](https://github.com/user-attachments/assets/941b0a84-107f-43b6-b4ab-ca9bf879be20)

### You can also delete the session from its own view if you want. 

-------------------------------
#### Thank you :) 
###### 418 I'm a teapot
  
  
