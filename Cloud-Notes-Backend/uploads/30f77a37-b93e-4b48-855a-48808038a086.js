const ex= require('express');
const mong = require('mongoose');

const schema = new mong.Schema({
    courseName:String,
    faculty:String,
    credits:Number,
    studentsEnrolled:Number
});

const crs = mong.model("crs",schema);

const router = ex.Router();

router.post("/",async (req,res)=>{
    const crse = await crs.create(req.body);
    res.json(crse);
});


router.get("/",async (req,res)=>{
    const crse = await crs.find();
    res.json(crse);
});

router.put("/:id",async (req,res)=>{
    const crse = await crs.findByIdAndUpdate(
        req.params.id,
        req.body,
        {new:true}
    );
    res.json(crse);
});

router.delete("/:id",async (req,res)=>{
    const crse = await crs.findByIdAndDelete(req.params.id);
    res.json({secexx:"deleted!!!"});
});

router.get("/total",async (req,res)=>{
    const crse = await crs.find();
    let ttl=0;
    crse.forEach(t=>ttl+=t.studentsEnrolled);
    res.json({totalSttds:ttl});
});

module.exports = router;