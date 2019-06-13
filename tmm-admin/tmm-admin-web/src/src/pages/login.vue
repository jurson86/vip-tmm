<template>
	<transition name="fade">
        <div class="login">
            <div class="login-con">
                <Card :bordered="false">
                    <p slot="title"><Icon type="log-in"></Icon> TMM管理系统</p>
                    <div class="form-con">
                        <Form ref="formInline" :model="formInline" :rules="ruleInline">
                            <FormItem prop="userName">
                                <Input type="text" v-model="formInline.userName" placeholder="账号">
                                    <Icon type="person" :size="16" slot="prepend"></Icon>
                                </Input>
                            </FormItem>
                            <FormItem prop="passWord">
                                <Input type="password" v-model="formInline.passWord" placeholder="密码" @on-enter="handleSubmit('formInline')">
                                    <Icon type="locked" :size="14" slot="prepend"></Icon>
                                </Input>
                            </FormItem>
                            <FormItem>
                                <Button type="primary" long @click="handleSubmit('formInline')">登录</Button>
                            </FormItem>
                        </Form>
                    </div>
                </Card>
            </div>
        </div>
    </transition>
</template>

<script type="es6">
	import {mapMutations, mapGetters} from 'vuex'

	export default{
		data(){
			return{
				formInline: {
                    userName: '',
                    passWord: ''
                },
                ruleInline: {
                    userName: [
                        {required: true, message: '请输入账号', trigger: 'blur'}
                    ],
                    passWord: [
                        {required: true, message: '请输入密码', trigger: 'blur'},
                        {
                            type: 'string',
                            min: 3,
                            message: '密码长度不能小于3位',
                            trigger: 'blur'
                        }
                    ]
                }
			}
		},

		computed: {
            ...mapGetters([
                'token'
            ])
        },

        created(){
        	if(this.token){
        		this.toLogin();
        	}
        },

        watch: {
        	token(newval){
        		if(newval){
        			this.toLogin();
        		}
        	}
        },

		methods: {
			handleSubmit(name) {
                this.$refs[name].validate((valid) => {
                    if (valid) {
                        this.$post('/login', this.formInline, true).then(res => {
                            if (res.code === 200) {
                                this.login(res.data.token);
                                this.setUserInfo(res.data.user);
                                this.$router.push(this.$route.query.redirect ? this.$route.query.redirect : '/home')
                                this.$Message.success({
                                    content: '登录成功!'
                                });
                            } else {
                                this.$Message.error(res.msg);
                            }
                        })
                    }
                })
            },

            toLogin(){
            	this.$router.push(this.$route.query.redirect ? this.$route.query.redirect : '/home');
            },

            ...mapMutations({
                login: 'HAS_TOKEN',
                setUserInfo: 'SET_USERINFO'
            })
		}
	}
</script>

<style lang="scss">
    .login{
        width: 100%;
        height: 100%;
        background-image: url('../assets/imgs/bg.jpg');
        background-size: cover;
        background-position: center;
        position: relative;
        &-con{
            position: absolute;
            right: 160px;
            top: 50%;
            transform: translateY(-60%);
            width: 320px;
            &-header{
                font-size: 16px;
                font-weight: 300;
                text-align: center;
                padding: 30px 0;
            }
            .form-con{
                padding: 10px 0 0;
            }
        }

        .ivu-card-head{
            border-top-left-radius: 4px;
            border-top-right-radius: 4px;
        }

        .ivu-form-item:nth-child(3) {
            .ivu-form-item-content {
                position: relative;
                .ivu-input-wrapper {
                    position: relative;
                    width: 150px;
                }
                img {
                    display: block;
                    width: 120px;
                    height: 32px;
                    position: absolute;
                    right: 0;
                    top: 0;
                    border-radius: 4px;
                    cursor: pointer;
                    text-align: center;
                    font-size: 14px;
                    color: #fff;
                }
            }
        }
    }
</style>
