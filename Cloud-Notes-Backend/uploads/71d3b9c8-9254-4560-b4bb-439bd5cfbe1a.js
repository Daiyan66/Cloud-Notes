// Explain various debugging and logging methods in Node.js with example to demonstrates the following:
// 1. Measuring execution time of code blocks.
// 2. Grouping related log messages and displaying them hierarchically.
// 3. Using assertions to conditionally display error messages.
// 4. Displaying data in a tabular format.
// 5. Logging debugging details for analysis

console.time('time');

console.group('main group');
console.group('sub group');
console.log('msg 1');
console.warn('msg 2');
console.error('msg 3');
console.group('sub group');
console.groupEnd('main group');

let x =5;
console.assert(x>6,'this is false!!');
console.assert(x<6,'this is true!!');

const stds = [{name:'daiyan',age:21},{name:'Suziya',age:19},{name:'Bi Bi',age:18}]
console.table(stds);
console.table(stds,['age']);

const debug = 'this is getting debigged!!';
console.log("debug details : ",debug);

console.timeEnd('time');