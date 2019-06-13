<template>
	<div>
		<div class="mrl">
			<Breadcrumb>
				<BreadcrumbItem :to="{name: 'home'}">首页</BreadcrumbItem>
				<BreadcrumbItem :to="{name: 'dieMsg'}">死信消息</BreadcrumbItem>
			</Breadcrumb>
		</div>
		<div class="button-box">
			<Button type="info" class="mr" @click="resend">死信回发</Button>
		</div>
		<Table class="mrl" border stripe :loading="loading" :columns="columns" :data="tableData" @on-selection-change="getSelection"></Table>
		<Page class="page" :total="totalCount" :current="page" show-total @on-change="changePage"></Page>
	</div>
</template>

<script type="es6">

	export default{
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
						title: 'queueName',
						key: 'name'
					},
					{
						title: 'vhost',
						key: 'vhost'
					},
					{
						title: 'messages',
						key: 'messages'
					},
					{
						// title: '',
						render: (h, params) => {
							return h('Button', {
								props: {
									type: 'primary',
									size: 'small'
								},
								on: {
									click: () => {
										this.$router.push({
											name: 'dieMsgList',
											query: {
												state: 11,
												queueName: params.row.name
											}
										})
									}
								}
							}, '查看队列列表');
						}
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
				this.$get('/api/message/dlq/list').then(res => {
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

				if(this.selectionArr.length > 1){
					this.$Message.warning('只能选择一条数据！');
					return;
				}

				let queue = this.selectionArr[0].name;

				this.$Modal.confirm({
					title: '回发提示',
					content: '确认回发这些数据吗？',
					onOk: () => {
						this.$post('/api/message/dlq/resend', {queue: queue}, true).then(res => {
							if(res.status == 200){
								this.$Message.success('回发成功！');
								this.getTableData();
							}else{
								this.$Message.error(res.message);
							}
						})
					}
				})
			}

			// discard(){
			// 	if(this.selectionArr.length === 0){
			// 		this.$Message.warning('请至少选择一条数据！');
			// 		return;
			// 	}

			// 	if(this.selectionArr.length > 1){
			// 		this.$Message.warning('只能选择一条数据！');
			// 		return;
			// 	}
				
			// 	let queue = selectionArr[0].name;

			// 	this.$Modal.confirm({
			// 		title: '废弃提示',
			// 		content: '确认废弃这些数据吗？',
			// 		onOk: () => {
			// 			this.$post('/api/message/discard', {queue: queue}, true).then(res => {
			// 				if(res.status == 200){
			// 					this.$Message.success('废弃成功！');
			// 					this.getTableData();
			// 				}else{
			// 					this.$Message.error(res.message);
			// 				}
			// 			})
			// 		}
			// 	})
			// }
		}
	}
</script>