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
		
		const resul =  await got.post('http://localhost:8081/getCountriesInfo', {
				json: {
					condition: astheneia
				},
				responseType: 'json'
			});
		res.render('search', {
			results: resul.body.countriesInfo
		})
    }
    catch (err){        
		console.log(err);
        res.render('search',{results: []})
    }

}
module.exports = {homeGet, searchGet}
