const fs = require('fs');
const {MongoClient} = require('mongodb');

async function main(){
    const client = new MongoClient("mongodb://127.0.0.1:27017");

    try{
        const emp = JSON.parse(fs.readFileSync('emp.json','utf-8'));
        await client.connect();

        const db = client.db('company');
        const collection = db.collection('emp');

        await collection.insertMany(emp);
        await collection.updateOne({empId:101},{$set:{designation:"manager"}});

        allemp =await collection.find().toArray();
        fs.writeFileSync("updated_emps.json",JSON.stringify(allemp,null,2));

        console.log("Exported sucessfully!!");

    }catch(err){
        console.log(err);
    }finally{
        await client.close();
    }
}

main();