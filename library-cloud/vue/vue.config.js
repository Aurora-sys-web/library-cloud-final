// 跨域配置
module.exports = {
    devServer: {
        host: 'localhost',
        port: 9876,
        proxy: {
            '/api/user': {
                target: 'http://localhost:8081',
                changeOrigin: true,
                pathRewrite: { '^/api': '' }
            },
            '/api/book': {
                target: 'http://localhost:8082',
                changeOrigin: true,
                pathRewrite: { '^/api': '' }
            },
            '/api/bookwithuser': {
                target: 'http://localhost:8083',
                changeOrigin: true,
                pathRewrite: { '^/api': '' }
            },
            '/api/LendRecord': {
                target: 'http://localhost:8083',
                changeOrigin: true,
                pathRewrite: { '^/api': '' }
            },
            '/api/borrow': {
                target: 'http://localhost:8083',
                changeOrigin: true,
                pathRewrite: { '^/api': '' }
            },
            '/api/dashboard': {
                target: 'http://localhost:8083',
                changeOrigin: true,
                pathRewrite: { '^/api': '' }
            },
            '/api/forget': {
                target: 'http://localhost:8081',
                changeOrigin: true,
                pathRewrite: { '^/api': '' }
            },
            '/api': {
                target: 'http://localhost:8083',
                changeOrigin: true,
                pathRewrite: { '^/api': '' }
            }
        }
    }
}