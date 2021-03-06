const path = require('path');
const CopyPlugin = require('copy-webpack-plugin');

module.exports = {
  entry: ['./src/index.js', './src/index.less'],
  resolve: {
    extensions: ['.ts', '.js'] // add your other extensions here
  },
  module: {
    rules: [
      {
        parser: {
          amd: false
        }
      },
      {
        test: /\.js$/,
        enforce: 'pre',
        use: ['source-map-loader'],
      },
      {
        test: /\.ts?$/,
        use: [
          { loader: 'ts-loader' }
        ],
        exclude: /node_modules/,
      },
      {
        test: /\.js?$/,
        use: 'babel-loader',
        exclude: /node_modules/,
      },
      {
        test: /\.html$/,
        use: [
          {
            loader: "html-loader"
          }
        ]
      },
      {
        test: /\.css$/,
        use: [
            'style-loader',
            'css-loader',
        ]
      },
      {
        test: /\.less$/,
        use: [
           { loader: "style-loader" },
           { loader: "css-loader" },
           { loader: "less-loader", options: { javascriptEnabled: true } }
        ]
      }
    ],
  },
  devServer: {
    contentBase: "./src",
    hot: true,
    port: 9000,
    proxy: {
      '/solve-boggle': {
         target: {
            host: "localhost",
            protocol: 'http:',
            port: 8080
         }
      },
      '/get-boggle-board': {
        target: {
           host: "localhost",
           protocol: 'http:',
           port: 8080
        }
     }
   }
  }
};