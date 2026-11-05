const fs = require('fs');
const {MongoClient}= require('mongodb');

async function main(){
    const client = new MongoClient("mongodb://127.0.0.1:27017");

    try{
        const emp = JSON.parse(fs.readFileSync('emp.json','utf-8'));
        await client.connect();

        const collection = client.db('cmp').collection('emp');

        await collection.insertMany(emp);
        await collection.updateMany({Id:101},{$set:{designation:"manager"}});

        const allemp = await collection.find().toArray();
        fs.writeFileSync('updated.json',JSON.stringify(allemp,null,2));

        console.log("expotes!!");

    }catch(err){

    }finally{
        await client.close();
    }
}
main();