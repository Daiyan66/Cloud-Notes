const students = [
    { name: "Alice", marks: 92 },
    { name: "Bob", marks: 78 },
    { name: "Charlie", marks: 88 },
    { name: "Diana", marks: 95 },
    { name: "Ethan", marks: 67 }
];

const stdCGPA = students.map(student => ({
    name: student.name,
    cgpa: (student.marks / 10)
}));

const highCGPA = stdCGPA.filter(s => s.cgpa >= 9);

const count = highCGPA.reduce(count => count + 1, 0);

console.log("Students with CGPA 9 or higher:");
highCGPA.forEach(s => {
    console.log(`${s.name} — CGPA: ${s.cgpa}`);
});

console.log(`Total Count: ${count}`);
