import Vue from 'vue'
import Vuex from 'vuex'
import * as actions from './action'
import * as getters from './getters'
import state from './state'
import mutations from './mutations'
import createLogger from 'vuex/dist/logger'

Vue.use(Vuex)

// 开发环境开启，上线去掉
const debug = process.env.NODE_ENV !== 'production';

export default new Vuex.Store({
    state,
    mutations,
    actions,
    getters,
    strict: debug,
    plugins: debug ? [createLogger()] : []
})