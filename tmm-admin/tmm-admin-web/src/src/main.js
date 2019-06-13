// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue'
import App from './App'
import router from './router'
import store from './store'

// import axios from 'axios'
import ajax from './utils/ajax'

import iview from 'iview'
import 'iview/dist/styles/iview.css'

Vue.use(ajax)
Vue.use(iview)

;(function () {
    var token = localStorage.getItem('token'),
        userinfo = localStorage.getItem('userinfo')
    if (token) {
        store.commit("HAS_TOKEN", token)
    }
    ;
    if (userinfo) {
        store.commit('SET_USERINFO', JSON.parse(userinfo))
    }
}())

// 路由拦截
router.beforeEach((to, from, next) => {
    if (to.matched.some(record => record.meta.requiresAuth)) {
        if (!store.getters.token) {
            next({
                path: '/login',
                query: { redirect: to.fullPath }
            })
        } else {
            next()
        }
    } else {
        next()
    }
    iview.LoadingBar.start();
    next();
});

router.afterEach(route => {
    iview.LoadingBar.finish();
});



Vue.config.productionTip = false

/* eslint-disable no-new */
new Vue({
  el: '#app',
  router,
  store,
  components: { App },
  template: '<App/>'
})
