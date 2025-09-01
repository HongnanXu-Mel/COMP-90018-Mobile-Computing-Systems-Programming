# 🔥 手动导入Firebase数据指南

## 🚨 问题分析
由于Firebase权限设置过于严格，自动上传脚本无法工作。现在为您提供手动导入的解决方案。

## 📋 方法1：通过Firebase控制台手动导入（推荐）

### 步骤1：访问Firebase控制台
1. 打开浏览器，访问 [Firebase控制台](https://console.firebase.google.com/)
2. 选择项目 `comp90018-cd0d1`

### 步骤2：进入Firestore Database
1. 在左侧菜单中点击 **Firestore Database**
2. 如果提示创建数据库，选择 **开始使用**
3. 选择 **测试模式** 以允许读写操作

### 步骤3：创建集合和文档
1. 点击 **开始集合**
2. 集合ID输入：`restaurants`
3. 点击 **下一步**

### 步骤4：逐个添加餐厅数据
对于每个餐厅，需要创建以下字段：

#### 字段1：name (字符串)
- 字段ID：`name`
- 类型：`string`
- 值：餐厅名称

#### 字段2：address (字符串)
- 字段ID：`address`
- 类型：`string`
- 值：餐厅地址

#### 字段3：latitude (数字)
- 字段ID：`latitude`
- 类型：`number`
- 值：纬度坐标

#### 字段4：longitude (数字)
- 字段ID：`longitude`
- 类型：`number`
- 值：经度坐标

#### 字段5：category (字符串)
- 字段ID：`category`
- 类型：`string`
- 值：餐厅分类

#### 字段6：region (字符串)
- 字段ID：`region`
- 类型：`string`
- 值：所在区域

### 步骤5：重复添加所有餐厅
需要重复步骤4，为103家餐厅分别创建文档。

## 🛠️ 方法2：修改Firestore安全规则

### 步骤1：进入安全规则
1. 在Firestore Database页面
2. 点击 **规则** 标签

### 步骤2：修改规则
将规则改为：
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if true;
    }
  }
}
```

### 步骤3：发布规则
点击 **发布** 按钮

## 📊 餐厅数据统计

### 总数：103家餐厅

### CBD区域 (约40家)
包括：Grand Hotel Melbourne, La Camera, The Savoy Hotel, Rendezvous Hotel, Oriental Teahouse, Ginza Teppanyaki, Steakhouse Grill 66, Japanese Teppanyaki Inn, Flower Drum Restaurant, Red Spice Road, MoVida, Chapati CBD, Chocolate Buddha, Left Bank, Cookie, Grill Steak Seafood, MAMASITA, Rockpool Bar & Grill, Hopscotch, The Meat & Wine Co, Melba Restaurant, MoVida Aqui, Florentino, Rare Steakhouse, Pizze Societe, The Wharf Hotel, Amigos y Familia, The Metropolitan Hotel, The Bottom End, Longrain, Hofbräuhaus, Dock 37, Maha Restaurant, Imperial Hotel, Berth, The French Brasserie等

### 内城区 (约15家)
包括：The Aviary Hotel, Bedi's Indian Restaurant, Copacabana International, Brunetti Classico Carlton, Añada Bar & Restaurant, Naked For Satan, D.O.C Pizza & Mozzarella Bar, Cutler, Ladro, Witches In Britches, Blue Chillies, Kwan's Table, Saints Kitchen Pizzeria, Daneli's Food Emporium, Chez Olivier Le Bistro, White Oaks Saloon Bar & Dining等

### 外城区 (约48家)
包括：Box Hill区域 (VegieHut, Crust Pizza, Una Una, Moohan Korean BBQ, Bak Kut Teh King, Cheong-chun Sushi, The Wokkers等), Balwyn & Doncaster区域 (One Thai, Vegie Mum, Bite Me, Don't Tell Mama, Eastern Bell, Sweet Crown, Persian Halal, Bin 3, Goodfellas, Westfield Food Court, Woodfire and Stone等), Camberwell & Hawthorn区域 (Bar None, Amici Trattoria, Afghan Village, Little Thai Princess, Ignite Cafe, Chicken and Jokbal, Dish & Spoon, Baba Sus, Fiorelli, Charntra, Garage At Night, Pizza Republica, Chan Korean, Chapter21, Linger Cafe, Minori Cafe, Viet Table, Gracie Greco, Con Noi, Cielo Pizza等), Glen Waverley区域 (Black Flat Coffee, YOMG, Shira Nui, Jumbo Buns, ATHURA, Caesars Veggie, Master Lanzhou等)

## ⚡ 快速导入技巧

### 批量复制字段
1. 创建第一个餐厅文档后
2. 复制该文档
3. 修改餐厅名称和地址
4. 重复此过程

### 使用JSON数据
1. 打开 `restaurants_data.json` 文件
2. 参考其中的数据结构
3. 按照相同格式在Firebase中创建

## 🔍 验证导入结果

### 检查方法
1. 在Firestore Database中查看 `restaurants` 集合
2. 应该看到103个餐厅文档
3. 每个文档包含6个字段

### 测试读取
1. 运行您的Android应用
2. 进入地图页面
3. 应该能看到橙色餐厅标记
4. 点击标记显示餐厅信息

## 🚀 导入完成后

数据导入成功后：
- ✅ Android应用能正常从Firebase读取数据
- ✅ 地图显示所有餐厅标记
- ✅ 点击标记显示餐厅名称和地址
- ✅ 不再显示"加载数据失败"错误

## 📝 注意事项

- 修改安全规则后，任何人都可以读写数据（仅用于测试）
- 生产环境中应该设置更严格的权限规则
- 手动导入需要耐心，但只需要做一次
- 如果遇到问题，可以删除集合重新开始
- **预计导入时间：30-45分钟**（103家餐厅）
- 建议分批导入：先导入20-30家测试，确认无误后再导入剩余

## 🆘 需要帮助？

如果手动导入过程中遇到问题：
1. 检查Firebase控制台是否有错误提示
2. 确认安全规则已正确设置
3. 验证字段类型和名称是否正确
4. 重新创建有问题的文档
