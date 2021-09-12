const express = require('express');
const bodyParser=require('body-parser')

require('dotenv').config()
const sqlite3 =require('sqlite3')
const open = require('sqlite').open 


const userController = require('./controllers/user.controller');
const healthController = require('./controllers/health.controller');
const authController = require('./controllers/auth.controller');
const msgController = require('./controllers/message.controller');

open({
  filename: process.env.DB_FILENAME,
  mode: sqlite3.OPEN_READWRITE,
  driver: sqlite3.Database

}).then((db) => {
  console.log(`Successfully connected to database: ${JSON.stringify(db)}`)
  let app = express()


  app
  .use(bodyParser.urlencoded({extended: true}))
  .use(bodyParser.json())
  .use((req, res, next) => {
    req.db = db
    next()
  })

  const port = process.env.PORT || 8080;
  app.post('/check', healthController.check);
  app.post('/user',  userController.createUser);
  app.post('/login', authController.login);
  
  // TODO: these endpoints should be secured
  app.post('/messages', msgController.send);
  app.get('/messages',  msgController.get);
  
  
  
  app.listen(port, () => {
    console.log(`ASAPP Challenge app running on port ${port}`);
  });
  
  
     



}).catch(err => {
  console.log(`Unable to connect to database at ${process.env.DB_FILENAME}. Error: ${err}`)
})






