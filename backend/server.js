require("dotenv").config();
const express = require("express");
const mongoose = require("mongoose");
const cors = require("cors");
const sessionRoutes = require("./routes/sessions");

const app = express();
const port = process.env.PORT || 3000;

app.use(express.json());
app.use(cors());
mongoose
  .connect(process.env.MONGO_URI)
  .then(() => console.log("Connected to MongoDB Atlas"))
  .catch((err) => console.error("MongoDB connection error:", err));

app.use("/api/sessions", sessionRoutes);

app.get("/", (req, res) => {
  res.send("Welcome to the Sensor Data API");
});

app.listen(port, () => {
  console.log(`Server running on port ${port}`);
});
