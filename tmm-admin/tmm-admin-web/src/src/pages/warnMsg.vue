<template>
	<div>
		<div class="mrl">
			<Breadcrumb>
				<BreadcrumbItem :to="{name: 'home'}">首页</BreadcrumbItem>
				<BreadcrumbItem :to="{name: 'warnMsg'}">异常消息</BreadcrumbItem>
			</Breadcrumb>
		</div>
		<div class="button-box">
			<Button type="info" class="mr" @click="resend">重新回调</Button>
			<Button type="error" class="mr" @click="discard">废弃</Button>
			<Button type="primary" @click="warnCheck">check</Button>
		</div>
		<Table class="mrl" border stripe :loading="loading" :columns="columns" :data="tableData" @on-selection-change="getSelection"></Table>
		<Page class="page" :total="totalCount" :current="page" show-total @on-change="changePage"></Page>
	</div>
</template>

<script type="es6">
	import messageDetail from '../components/message-detail.vue';

	export default{
		components: { messageDetail },

		data(){
			return{
				loading: false,
				page: 1,
				pageSize: 10,
				selectionArr: [],
				totalCount: 0,
				totalData: [],
				tableData: [],
				columns: [					
					{
						type: 'selection',
						width: 60,
						align: 'center'
					},
					{
                    	type: 'expand',
                    	width: 50,
                    	render: (h, params) => {
                    		return h(messageDetail, {
                    			props: {
                    				pid: params.row.pid
                    			}
                    		})
                    	}
                    },
					{
						title: 'uId',
						key: 'uId'
					},
					{
						title: 'pid',
						key: 'pid'
					},
					{
						title: 'clusterIp',
						key: 'clusterIp'
					},
					{
						title: 'serviceName',
						key: 'serviceName'
					},
					{
						title: 'updateTime',
						key: 'updateTime'
					},
					{
						title: 'messageTopic',
						key: 'messageTopic'
					},
					{
						title: 'messageState',
						key: 'messageState',
						render: (h, params) => {
							return h('p', '异常消息');
						}
					},
					{
						title: 'presendBackUrl',
						key: 'presendBackUrl'
					}
				]
			}
		},

		created(){
			this.getTableData();
		},

		methods: {
			getSelection(selection){
				this.selectionArr = selection;
			},

			warnCheck(){
				if(this.selectionArr.length === 0){
					this.$Message.warning('请至少选择一条数据！');
					return;
				}

				let array = [];
				this.selectionArr.map(item => {
					array.push(item.pid);
				});

				this.$router.push({
					name: 'warnCheck',
					query: {
						ids: array
					}
				})
			},

			changePage(cur){
				this.page = cur;
				this.tableData = [];				
				let i = this.page > 1 ? (this.page - 1) * this.pageSize : 0;
				let len = this.totalData.length - i < this.pageSize ? this.totalData.length : (this.page * this.pageSize);
				for(; i < len; i++){
					this.tableData.push(this.totalData[i]);
				}
			},

			getTableData(){
				this.loading = true;
				this.$get('/api/messageList/state', {
					state: 21
				}, true).then(res => {
					this.loading = false;
					if(res.status == 200){
						this.tableData = [];
						this.totalData = res.data;
						this.totalCount = res.data.length;
						if(this.totalCount > this.pageSize){
							for(let i = 0; i < this.pageSize; i++){
								this.tableData.push(res.data[i]);
							}
						}else{
							this.tableData = res.data;
						}
					}else{
						this.$Message.warning(res.msg);
					}
				})
			},

			resend(){
				if(this.selectionArr.length === 0){
					this.$Message.warning('请至少选择一条数据！');
					return;
				}

				let array = [];
				this.selectionArr.map(item => {
					array.push(item.pid);
				});

				let arrStr = array.toString();

				this.$Modal.confirm({
					title: '回调提示',
					content: '确认回调这些数据吗？',
					onOk: () => {
						this.$post('/api/message/resend', {pids: arrStr}, true).then(res => {
							if(res.status == 200){
								this.$Message.success('回调成功！');
								this.getTableData();
							}else{
								this.$Message.error(res.message);
							}
						})
					}
				})
			},

			discard(){
				if(this.selectionArr.length === 0){
					this.$Message.warning('请至少选择一条数据！');
					return;
				}

				let array = [];
				this.selectionArr.map(item => {
					array.push(item.pid);
				});

				let arrStr = array.toString();

				this.$Modal.confirm({
					title: '废弃提示',
					content: '确认废弃这些数据吗？',
					onOk: () => {
						this.$post('/api/message/discard', {pids: arrStr}, true).then(res => {
							if(res.status == 200){
								this.$Message.success('废弃成功！');
								this.getTableData();
							}else{
								this.$Message.error(res.message);
							}
						})
					}
				})
			}
		}
	}
</script>