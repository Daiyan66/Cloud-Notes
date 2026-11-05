// Illustrate how to add an event listener, emit an event to trigger the listener and then remove the event listener in node.js.

const Events = require('events');

const emitter = new Events();

emitter.off('hi',()=>{
    console.log('this is a msg!!!');
})

// emitter.on('hi',()=>{
//     console.log('this is a msg!!!');
// })

emitter.emit('hi');