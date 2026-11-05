// Write a Node.js program to:
// 1. Create an HTTP server that listens on port 8000.
// 2. Respond with “Welcome to Nodejs” when the root URL (/) is accessed.
// 3. Return a 404 status with a custom message for all other routes.

const http = require('http');

const server = http.createServer((req,res)=>{
    if(req.url==='/'){
        res.end('Welcome to Nodejs!!!');
    }else{
        res.end('No data on this page!!!');
    }
})

server.listen(8080,console.log('thr port has stared!!'));