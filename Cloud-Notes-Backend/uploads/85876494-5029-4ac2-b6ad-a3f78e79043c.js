// Apply the concept of custom modules in Node.js.
// 1. Create a custom module fileOperations.js with functions to read a file, write to a file and append data to a file.
// 2. Use this module in another file app.js to demonstrate its functionality by performing file operations.

const funcs = require('./prog-14-A.js');


const d = "this is new data which will be writen in the file";
const a = "\nthis ill bw appended in the dtaa!!!"
funcs.read();
funcs.write(d);
funcs.append(a);
