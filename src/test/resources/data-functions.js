module.exports = { createCommandObject };
const {v4 : uuidv4} = require('uuid')
const accounts = [];

//fill up with 10 unique account ids
for (let i = 0; i < 10; i ++ ){
  accounts[i] = uuidv4();
}
console.log('generated test accounts: ' + accounts)
let count = 0;
function cycle(items) {
  let item = accounts[count];
  // increment our counter
  count++;
  // reset counter if we reach end of array
  if (count === items.length) {
    count = 0;
  }
  return item;
}

function createCommandObject(userContext, events, done) {
  const v = Math.floor(Math.random() * 100);
  let user = undefined;
  if (userContext.vars.data != undefined) {
    user = userContext.vars.data.account_id;
  }
  if(user == undefined) {
    user = cycle(accounts);
  }
  const data = { account_id : user, cmd: v % 2 == 0 ? "ADD" : "SUBTRACT", value: v };
  // set the "data" variable for the virtual user to use in the subsequent action
  userContext.vars.data = data;
  return done();
}
