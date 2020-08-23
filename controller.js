const got = require('got');
const fs = require('fs')
const xml2js = require('xml2js');
const util = require('util')

function homeGet(req, res){
    res.render('home')
}

async function searchGet(req, res){
    try{
    let astheneia = req.query.astheneia
    const body = (await got('https://clinicaltrials.gov/ct2/results/download_fields?cond=' + astheneia + '&down_fmt=xml&down_count=10000')).body;   
    const json = await (util.promisify(xml2js.parseString))(body)
    let count = json.search_results.$.count
    
    let results = {}
    let setOfRecords = Math.ceil(count / 10000)

    for (let i = 0; i < count % 10000; i++) {
        let locations = json.search_results.study[i].locations[0].location;
        if (locations != undefined) {
            for (let j = 0; j < locations.length; j++) {
                let loc = locations[j].split(", ")
                loc = loc[loc.length - 1]
                if (results[loc] != null)
                    results[loc] += 1;
                else
                    results[loc] = 1;
            }
        }
    }
    for (let recordNo = 2; recordNo <= setOfRecords; recordNo++) {
        const body = (await got('https://clinicaltrials.gov/ct2/results/download_fields?cond=' + astheneia + '&down_fmt=xml&down_count=10000&down_chunk='+recordNo)).body;   
        const json = await (util.promisify(xml2js.parseString))(body)
        for (let i = 0; i < count % 10000; i++) {
            let locations = json.search_results.study[i].locations[0].location;
            if (locations != undefined) {
                for (let j = 0; j < locations.length; j++) {
                    let loc = locations[j].split(", ")
                    loc = loc[loc.length - 1]
                    if (results[loc] != null)
                        results[loc] += 1;
                    else
                        results[loc] = 1;
                }
            }
        }
    }

    var sortable = [];
    for (var loc in results) {
        sortable.push([loc, results[loc]]);
    }

    sortable.sort(function (a, b) {
        return b[1] - a[1];
    });
    res.render('search', {
        results: sortable
    })
    }
    catch (err){        
        res.render('search',{results: []})
    }
}

module.exports = {homeGet, searchGet}