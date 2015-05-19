var caramel = require('caramel');

caramel.configs({
    context: '/dashboard',
    cache: true,
    negotiation: true,
    themer: function () {
        return 'theme0';
    }
});
