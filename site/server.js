const https = require('https');
const path = require('path');
const express = require('express');
const config = require('./config.js');
const app = express();
const appDir = config.app.appDir;

//Global variable 
global.appDir = appDir;

app.set('port', config.app.port); // port, 443 probably
app.set('views', config.app.views);
app.set('view engine', config.app.viewEngine);

// Express configuration
app.use(express.json());       // to support JSON-encoded bodies.
app.use(express.urlencoded({extended: true})); // to support URL-encoded bodies
app.use(express.static(path.join(appDir, 'public'))); // configure express to use public folder

// routing
let router = require('./routing/routing.js');
app.use('/', router);

//module.exports = app;
// https.createServer(config.httpsOptions
//     ,app).listen(443);

 const http = require('http');
// http.createServer((req, res)=>{
//     res.writeHead(301, { "Location": "https://" + req.headers['host'] + req.url});
//     res.end();
// }).listen(80);


http.createServer(app).listen(config.app.port);
