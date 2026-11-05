// Apply the concept of custom modules in Node.js.
// 1. Create a custom module fileOperations.js with functions to read a file, write to a file and append data to a file.
// 2. Use this module in another file app.js to demonstrate its functionality by performing file operations.


const fs=require('fs');

function read(){
    fs.readFile('prog-13.txt','utf-8',(err,data)=>{if(err) throw new Error;
        else {console.log(data);}
    })
}

function write(data){
    fs.writeFile('prog-13.txt',data,(err)=>{
        if(err) throw new Error;
    })
}

function append(data){
    fs.appendFile('prog-13.txt',data,(err)=>{
        if(err) throw new Error;
    })
}



module.exports = {read,write,append};