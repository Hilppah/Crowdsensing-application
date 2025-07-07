const express = require("express");
const app = express();
const port = 3000;
const sensorRoutes = require("./routes/sensors");

require("dotenv").config();
const mongoose = require("mongoose");

mongoose
  .connect(process.env.MONGO_URI)
  .then(() => console.log("Connected to MongoDB Atlas"))
  .catch((err) => console.error("MongoDB connection error:", err));

app.use(express.json());
app.use("/api/sensors", sensorRoutes);

app.get("/", (req, res) => {
  res.send("Hello, World!");
});

app.listen(port, () => {
  console.log("Server started on http://localhost:" + port);
});
