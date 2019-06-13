<template>
	<div>
		<div class="mrl">
			<Breadcrumb>
				<BreadcrumbItem :to="{name: 'home'}">首页</BreadcrumbItem>
				<BreadcrumbItem :to="{name: 'clearMsg'}">数据清理</BreadcrumbItem>
			</Breadcrumb>
		</div>
		<Row class="button-box">
			<Col span="4">
				<Select v-model="params.state" @on-change="changeSelect">
					<Option v-for="item in stateArr" :value="item.id" :key="item.id">{{item.name}}</Option>
				</Select>
			</Col>
			<Col span="8">
				<DatePicker type="date" class="date-input" :value="params.startTime" format="yyyy-MM-dd HH:mm:ss" @on-change="changeBeginTm" placeholder="开始日期"></DatePicker>
				 -- 
				<DatePicker type="date" class="date-input" :value="params.endTime" format="yyyy-MM-dd HH:mm:ss" @on-change="changeEndTm" placeholder="结束日期"></DatePicker>
			</Col>
			<Col span="4">
				<Button type="ghost" @click="getTableData" class="mr">查询</Button>
				<Button type="info" @click="clearData">清理</Button>
			</Col>
		</Row>
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
				totalCount: 0,
				totalData: [],
				selectionArr: [],
				tableData: [],
				params: {
					state: 30,
					startTime: '',
					endTime: ''
				},
				stateArr: [
					{id: 30, name: '完成'},
					{id: 100, name: '废弃'}
				],
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
							let text = '';
							switch(params.row.messageState){
								case 30: text = '完成消息'; break;
								case 100: text = '废弃消息'; break;
							}
							return h('p', text);
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
			changeBeginTm(val){
				this.params.startTime = val;
			},

			changeEndTm(val){
				this.params.endTime = val;
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

			changeSelect(){
				this.page = 1;
				this.getTableData();
			},

			getSelection(selection){
				this.selectionArr = selection;
			},

			getTableData(){
				this.loading = true;
				let startTime = new Date(this.params.startTime).getTime() ? new Date(this.params.startTime).getTime() : '';
				let endTime = new Date(this.params.endTime).getTime() ? new Date(this.params.endTime).getTime() : '';

				this.$get('/api/messageList/state', {
					state: this.params.state,
					startTime: startTime,
					endTime: endTime
				}, true).then(res => {
					this.loading = false;
					if(res.status == 200){
						this.tableData = [];
						this.totalCount = res.data.length;
						this.totalData = res.data;
						if(this.totalCount > this.pageSize){
							for(let i = 0; i < this.pageSize; i++){
								this.tableData.push(res.data[i]);
							}
						}else{
							this.tableData = res.data;
						}
					}
				})
			},

			clearData(){
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
					title: '删除提示',
					content: '确认删除这些数据吗？',
					onOk: () => {
						this.$post('/api/message/delete', {pids: arrStr}, true).then(res => {
							if(res.status == 200){
								this.$Message.success('删除成功！');
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