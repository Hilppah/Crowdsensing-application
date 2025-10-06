require("dotenv").config();
const express = require("express");
const mongoose = require("mongoose");
const cors = require("cors");
const sessionRoutes = require("./routes/sessions");

const app = express();
const port = process.env.PORT || 3000;

app.use(express.json({ limit: "100mb" }));
app.use(express.urlencoded({ limit: "100mb", extended: true }));
aapp.use(cors({
  origin: "*",
  methods: ["GET", "POST", "DELETE", "OPTIONS"],
  allowedHeaders: ["Content-Type", "Authorization"],
}));

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
