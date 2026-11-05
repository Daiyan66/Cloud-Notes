const ex = require('express');
const app = ex();
app.use(ex.json());

const teach = [];
let id=1;

app.post("/teach",(req,res)=>{
  const {name,dept,exp,email}=req.body;
  if(!name,!dept,!exp,!email) return res.json({error:"all fields required"});
  const tea={id:id++,name,dept,exp,email};
  teach.push(tea);
  res.json(tea);
});

app.get("/teach",(req,res)=>{
  res.json({teachers:teach});
});

app.get("/teach/:id",(req,res)=>{
  const tea = teach.find(t=>t.id==req.params.id);
  if(!tea) return res.json({error:"Not found"});
  res.json(tea);
});

app.put("/teach/:id",(req,res)=>{
  const tea = teach.find(t=>t.id==req.params.id);
  if(!tea) return res.json({error:"Not found"});
  Object.assign(tea,req.body);
  res.json(tea);
});

app.delete("/teach/:id",(req,res)=>{
    const index = teach.findIndex(t=>t.id==req.params.id);
    if(index===-1) return res.json({error:"Not Found!!"});
    teach.splice(index,1);
    res.json({Sucess:"Teacher deleted suxccessfully!!"});
})

app.listen(3000,console.log("Started"));