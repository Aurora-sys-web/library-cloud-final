module.exports = {
    devServer: {
        host: 'localhost',
        port: 9876,
        proxy: {
            '/api/user': {
                target: 'http://127.0.0.1:8081',
                changeOrigin: true,
                pathRewrite: { '^/api': '' }
            },
            '/api/book': {
                target: 'http://127.0.0.1:8082',
                changeOrigin: true,
                pathRewrite: { '^/api': '' }
            },
            '/api/bookwithuser': {
                target: 'http://127.0.0.1:8083',
                changeOrigin: true,
                pathRewrite: { '^/api': '' }
            },
            '/api/LendRecord': {
                target: 'http://127.0.0.1:8083',
                changeOrigin: true,
                pathRewrite: { '^/api': '' }
            },
            '/api/borrow': {
                target: 'http://127.0.0.1:8083',
                changeOrigin: true,
                pathRewrite: { '^/api': '' }
            },
            '/api/dashboard': {
                target: 'http://127.0.0.1:8083',
                changeOrigin: true,
                pathRewrite: { '^/api': '' }
            },
            '/api/forget': {
                target: 'http://127.0.0.1:8081',
                changeOrigin: true,
                pathRewrite: { '^/api': '' }
            },
            '/api': {
                target: 'http://127.0.0.1:8083',
                changeOrigin: true,
                pathRewrite: { '^/api': '' }
            }
        }
    }
}