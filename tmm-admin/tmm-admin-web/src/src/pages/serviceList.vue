<template>
	<div>
		<div class="mrl">
			<Breadcrumb>
				<BreadcrumbItem :to="{name: 'home'}">首页</BreadcrumbItem>
				<BreadcrumbItem :to="{name: 'serviceList'}">服务列表</BreadcrumbItem>
			</Breadcrumb>
		</div>
		<Table class="mrl" border stripe :loading="loading" :columns="columns" :data="tableData" @on-selection-change="getSelection"></Table>
		<Page class="page" :total="totalCount" :current="page" show-total @on-change="changePage"></Page>
	</div>
</template>

<script type="es6">
	import serviceDetail from '../components/service-detail.vue';

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
					// {
					// 	type: 'selection',
					// 	width: 60,
					// 	align: 'center'
					// },
					{
                    	type: 'expand',
                    	width: 50,
                    	render: (h, params) => {
                    		return h(serviceDetail, {
                    			props: {
                    				monitor: params.row.monitor
                    			}
                    		})
                    	}
                    },
                    {
                    	title: 'serviceName',
                    	key: 'serviceName'
                    },
                    {
                    	title: 'url',
                    	key: 'url'
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
				this.$get('/api/monitor/agent').then(res => {
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
						this.$Message.warning(res.message);
					}
				})
			}
		}
	}
</script>