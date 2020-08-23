const path = require('path');
var appDir = path.dirname(__dirname)
const fs = require('fs');
const config = {
    app : {
        port: 80,
        views : './views',
        viewEngine : 'ejs',
        appDir : appDir+'/site'
    },

}

module.exports = config;
