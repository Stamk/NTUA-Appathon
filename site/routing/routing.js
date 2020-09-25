var express = require('express');
var router = express.Router();
const fs = require('fs')
/* ------- Load Controllers ----------- */
const {homeGet, searchGet} = require(appDir+'/controller.js')

router.get('/', homeGet);
router.get('/search', searchGet);
/* ---------- Error Page ---------- */
router.all('*', (req, res)=>{
    res.send("Wrong page")
    res.end();
})
module.exports = router;
