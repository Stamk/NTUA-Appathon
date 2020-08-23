const https = require('https');
const path = require('path');
const express = require('express');
const config = require('./config.js');
const app = express();
const appDir = config.app.appDir;

//Global variable 
global.appDir = appDir;

app.set('port', config.app.port); // port, 80 probably
app.set('views', config.app.views);
app.set('view engine', config.app.viewEngine);

// Express configuration
app.use(express.json());       // to support JSON-encoded bodies.
app.use(express.urlencoded({extended: true})); // to support URL-encoded bodies
app.use(express.static(path.join(appDir, 'public'))); // configure express to use public folder

// routing
let router = require('./routing/routing.js');
app.use('/', router);



 const http = require('http');



http.createServer(app).listen(config.app.port);
