import axios from 'axios';
import qs from 'qs';
import Vue from 'vue';
import store from '../store';
import modal from 'iview/src/components/modal/modal.vue';

Vue.component(modal.name, modal);

axios.defaults.baseURL = '/api';

// http request 拦截器
axios.interceptors.request.use(
    config => {
        if (store.getters.token) {  // 判断是否存在token，如果存在的话，则每个http header都加上token
            config.headers.token = `${store.getters.token}`;
        }
        return config;
    },
    err => {
        return Promise.reject(err);
    });

// http response 拦截器
axios.interceptors.response.use(
    response => {
        if (response.data.code == 5003) {
            // 返回 401 清除token信息并跳转到登录页面
            store.commit('LOGIN_OUT');

            new Vue().$Modal.confirm({
                title: '登录已过期',
                content: '<p>您的登录信息已过期，请重新登录</p><p>点击取消可复制当前页面重要信息</p>',
                okText: '去登录',
                cancelText: '取消',
                onOk: () => {
                    //history mode
                    // window.location.replace(`/#/login?redirect=${location.pathname}${location.search}`);

                    //hash mode
                    window.location.replace('/#/login?redirect=' + location.hash.slice(1));
                }
            })
        }

        return response;
    },
    error => {
        if (error.response) {
            console.log(error.response)
        }
        return Promise.reject(error)   // 返回接口返回的错误信息
    });


export default{
	install(Vue, option){
		Vue.prototype.$post = function(url, params, sb){
			return new Promise((resolve, reject) => {
				axios.post(url, sb ? qs.stringify(params) : params).then(res => {
					resolve(res.data);
				}, err => {
					reject(err);
				}).catch(error => {
					console.info(error);
				})
			})
		}

		Vue.prototype.$get = function (url, params) {
            const newParams = Object.assign({noCache: new Date().getTime()}, params);

            return new Promise((resolve, reject) => {
                axios.get(url, {params: newParams}).then(res => {
                    resolve(res.data)
                }, err => {
                    reject(err)
                }).catch(error => {
                    console.log(error);
                })
            })
        }
	}
}