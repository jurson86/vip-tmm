import * as types from './mutations-type'

const mutations = {
	[types.HAS_TOKEN](state, payload) {
        state.token = payload;
        window.localStorage.setItem('token', payload);
    },
    [types.LOGIN_OUT](state) {
        state.token = null;
        window.localStorage.removeItem('token');
        window.localStorage.removeItem('userinfo')
    },
    [types.SET_USERINFO](state,payload) {
        state.userinfo = payload;
        window.localStorage.setItem('userinfo', JSON.stringify(payload));
    }
}

export default mutations;