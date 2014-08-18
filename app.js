var caramel = require('caramel');

caramel.configs({
    context: '/as',
    cache: true,
    negotiation: true,
    themer: function () {
        return 'theme0';
    }
});