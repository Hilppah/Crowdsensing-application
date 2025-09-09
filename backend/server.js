require("dotenv").config();
const express = require("express");
const mongoose = require("mongoose");
const sessionRoutes = require("./routes/sessions");

const app = express();
const port = 3000;

app.use(express.json());

mongoose
  .connect(process.env.MONGO_URI)
  .then(() => console.log("Connected to MongoDB Atlas"))
  .catch((err) => console.error("MongoDB connection error:", err));

app.use(express.json());
app.use("/api/sessions", sessionRoutes);


app.get("/", (req, res) => {
  res.send("Welcome to the Sensor Data API");
});

app.listen(port, () => {
  console.log(`Server running on http://localhost:${port}`);
});
