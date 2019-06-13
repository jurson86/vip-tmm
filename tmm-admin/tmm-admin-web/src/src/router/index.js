import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router)

const Login = () => import(/* webpackChunkName: "group-backstage" */ '../pages/login.vue')
const BackStage = () => import(/* webpackChunkName: "group-backstage" */ '../pages/backstage.vue')
const Home = () => import(/* webpackChunkName: "group-backstage" */ '../pages/home.vue')
const WarnMsg = () => import(/* webpackChunkName: "group-backstage" */ '../pages/warnMsg.vue')
const DeathMsg = () => import(/* webpackChunkName: "group-backstage" */ '../pages/deathMsg.vue')
const DieMsg = () => import(/* webpackChunkName: "group-backstage" */ '../pages/dieMsg.vue')
const ClearMsg = () => import(/* webpackChunkName: "group-backstage" */ '../pages/clearMsg.vue')
const HelpCenter = () => import(/* webpackChunkName: "group-backstage" */ '../pages/helpCenter.vue')
const DieMsgList = () => import(/* webpackChunkName: "group-backstage" */ '../pages/dieMsgList.vue')
const ServiceList = () => import(/* webpackChunkName: "group-backstage" */ '../pages/serviceList.vue')

const ModelView = () => import(/* webpackChunkName: "group-backstage" */ '../pages/view.vue')

const UserList = () => import(/* webpackChunkName: "group-user" */ '../pages/user/userList.vue')
const UserEdit = () => import(/* webpackChunkName: "group-user" */ '../pages/user/userEdit.vue')

const RoleList = () => import(/* webpackChunkName: "group-role" */ '../pages/role/roleList.vue')
const RoleEdit = () => import(/* webpackChunkName: "group-role" */ '../pages/role/roleEdit.vue')

const ApplicationList = () => import(/* webpackChunkName: "group-service" */ '../pages/service/serviceList.vue')
const ApplicationEdit = () => import(/* webpackChunkName: "group-service" */ '../pages/service/serviceEdit.vue')
const ServiceQueList = () => import(/* webpackChunkName: "group-service" */ '../pages/service/serviceQueList.vue')

const MQList = () => import(/* webpackChunkName: "group-mqlist" */ '../pages/mqList/mqList.vue')
const MQEdit = () => import(/* webpackChunkName: "group-mqlist" */ '../pages/mqList/mqEdit.vue')

const WarnCheck = () => import(/* webpackChunkName: "group-warnMsg" */ '../pages/warnMsg/check.vue')

export default new Router({
	routes: [
	    {
            path: '/',
            component: Login,
        },
        {
            path: '/login',
            name: 'login',
            component: Login
        },
	    {
	        path: '/',
	        // name: 'backstage',
	        component: BackStage,
	        meta: {
                requiresAuth: true
            },
	        redirect: '/home',
	        children: [
	        	{
	        		path: '/home',
	        		name: 'home',
	        		component: Home
	        	},
	        	{
	        		path: '/warnMsg',
	        		component: ModelView,
	        		children: [
	        			{
	        				path: '',
	        				name: 'warnMsg',
	        				component: WarnMsg
	        			},
	        			{
	        				path: 'warnCheck',
	        				name: 'warnCheck',
	        				component: WarnCheck
	        			}
	        		]
	        	},
	        	{
	        		path: '/deathMsg',
	        		name: 'deathMsg',
	        		component: DeathMsg
	        	},
	        	{
	        		path: '/dieMsg',
	        		name: 'dieMsg',
	        		component: DieMsg
	        	},
	        	{
	        		path: '/clearMsg',
	        		name: 'clearMsg',
	        		component: ClearMsg
	        	},
	        	{
	        		path: '/helpCenter',
	        		name: 'helpCenter',
	        		component: HelpCenter
	        	},
	        	{
	        		path: '/dieMsgList',
	        		name: 'dieMsgList',
	        		component: DieMsgList
	        	},
	        	{
	        		path: '/serviceList',
	        		name: 'serviceList',
	        		component: ServiceList
	        	},
	        	{
	        		path: '/userList',
	        		component: ModelView,
	        		children: [
	        			{
	        				path: '',
	        				name: 'userList',
	        				component: UserList
	        			},
	        			{
	        				path: 'add',
	        				name: 'userAdd',
	        				component: UserEdit
	        			},
	        			{
	        				path: 'edit',
	        				name: 'userEdit',
	        				component: UserEdit
	        			}
	        		]
	        	},
	        	{
	        		path: '/roleList',
	        		component: ModelView,
	        		children: [
	        			{
	        				path: '',
	        				name: 'roleList',
	        				component: RoleList
	        			},
	        			{
	        				path: 'add',
	        				name: 'roleAdd',
	        				component: RoleEdit
	        			},
	        			{
	        				path: 'edit/:id',
	        				name: 'roleEdit',
	        				component: RoleEdit
	        			}
	        		]
	        	},
	        	{
	        		path: '/applicationList',
	        		component: ModelView,
	        		children: [
	        			{
	        				path: '',
	        				name: 'applicationList',
	        				component: ApplicationList
	        			},
	        			{
	        				path: 'add',
	        				name: 'applicationAdd',
	        				component: ApplicationEdit
	        			},
	        			{
	        				path: 'edit/:id',
	        				name: 'applicationEdit',
	        				component: ApplicationEdit
	        			},
	        			{
	        				path: 'serviceQueList/:name',
	        				name: 'serviceQueList',
	        				component: ServiceQueList
	        			}
	        		]
	        	},
	        	{
	        		path: '/mqList',
	        		component: ModelView,
	        		children: [
	        			{
	        				path: '',
	        				name: 'mqList',
	        				component: MQList
	        			},
	        			{
	        				path: 'add',
	        				name: 'mqAdd',
	        				component: MQEdit
	        			},
	        			{
	        				path: 'edit/:id',
	        				name: 'mqEdit',
	        				component: MQEdit
	        			}
	        		]
	        	}
	        ]
	    }
	]
})
