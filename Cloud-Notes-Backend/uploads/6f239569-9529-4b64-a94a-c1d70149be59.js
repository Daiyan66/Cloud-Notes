//npm install express mongoose

const ex = require('express');
const mong = require('mongoose');
const app=ex();
app.use(ex.json());

mong.connect("mongodb://127.0.0.1:27017/crs").then(console.log("Mongo Connected!!"));

app.use("/crs",require("./cr"));
app.listen(3000,console.log("Sever started!!"));