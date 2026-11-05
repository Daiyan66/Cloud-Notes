// Write a Node.js program to read input from the console and store it in a file using file server module.

const readline = require('readline').createInterface({
    input:process.stdin,
    output:process.stdout}
);
const fs = require('fs');

function something(){
    readline.question("Enter the Name :- ",(name)=>{
        readline.question("Eter the age :- ",(age)=>{
            readline.question('Enter the clg name :- ',(clg)=>{
                const data = `name:- ${name}\n Age:- ${age} \n Collage :- ${clg}`;
                fs.writeFile('prog-13.txt',data,(err)=>{if(err)throw new Error;});
                readline.close();
            })
        })
    })
}

something();