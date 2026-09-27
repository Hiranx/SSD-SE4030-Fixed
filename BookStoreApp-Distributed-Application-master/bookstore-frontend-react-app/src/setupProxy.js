module.exports = function (app) {
  app.use(function (req, res, next) {
    res.setHeader('X-Frame-Options', 'DENY');
    next();
  });
};
