const health = require('../models/health')
const fs = require("fs")

/**
 * Checks if the app is running fine.
 */
module.exports.check = async (req, res) => {
  
  const dbIsWorking = fs.existsSync(process.env.DB_FILENAME)
  dbIsWorking ? res.json(health) : res.json({health: "Database Error"});
}