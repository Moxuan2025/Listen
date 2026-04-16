const express = require('express');
const cors = require('cors');
const multer = require('multer');

const app = express();
const upload = multer(); // 只解析 multipart，不存文件

// 中间件
app.use(cors());               // 允许跨域，方便前端调试
app.use(express.json());       // 解析 application/json

// ---------- 通用返回包装 ----------
const success = (data) => ({ code: 0, message: 'ok', data });
const error = (code, message) => ({ code, message, data: null });

// ---------- Mock 数据 ----------
const MOCK_USER = {
  userId: 'u001',
  name: '张三',
  role: 'guardian',
  token: 'mock-jwt-token-123456'
};

const MOCK_CHILDREN = [
  { childId: 'c001', name: '小明', age: 6 },
  { childId: 'c002', name: '小红', age: 5 }
];

const MOCK_PLANS = [
  { planId: 'p001', name: '基础发音训练', status: 'ongoing' },
  { planId: 'p002', name: '进阶拼读', status: 'pending' }
];

const MOCK_TASKS = [
  { taskId: 't001', text: 'apple', phoneme: 'æ', difficulty: 1 },
  { taskId: 't002', text: 'cat', phoneme: 'æ', difficulty: 1 },
  { taskId: 't003', text: 'dog', phoneme: 'ɔ', difficulty: 2 }
];

// ---------- 路由 ----------

// 1. 登录
app.post('/api/v1/auth/login', (req, res) => {
  const { username, password } = req.body;
  // 完全不校验，直接返回成功
  res.json(success(MOCK_USER));
});

// 2. 获取当前用户
app.get('/api/v1/auth/me', (req, res) => {
  // 忽略 token，直接返回固定用户
  const { token, ...userWithoutToken } = MOCK_USER;
  res.json(success(userWithoutToken));
});

// 3. 获取孩子列表
app.get('/api/v1/children', (req, res) => {
  res.json(success(MOCK_CHILDREN));
});

// 4. 绑定孩子
app.post('/api/v1/children/bind', (req, res) => {
  // 直接返回成功，不检查 childId 是否存在
  res.json(success({ success: true }));
});

// 5. 获取训练计划
app.get('/api/v1/plans', (req, res) => {
  // 忽略 childId 参数，直接返回全部计划
  res.json(success(MOCK_PLANS));
});

// 6. 获取今日任务
app.get('/api/v1/tasks/today', (req, res) => {
  // 忽略 childId 和 planId
  res.json(success(MOCK_TASKS));
});

// 7. 上传音频（极简版，只接收文件并返回假 audioId）
app.post('/api/v1/audio/upload', upload.single('file'), (req, res) => {
  // 这里不处理文件内容，只返回一个假 ID
  const audioId = 'audio_' + Date.now();
  res.json(success({
    audioId: audioId,
    url: `http://192.168.x.x:3000/fake-audio/${audioId}.wav`
  }));
});

// 8. 评分接口
app.post('/api/v1/score/evaluate', (req, res) => {
  // 忽略 audioId 等参数，直接返回假评分结果
  res.json(success({
    recordId: 'rec_' + Date.now(),
    score: 86,
    accuracy: 88,
    fluency: 80,
    feedback: '发音基本正确，但尾音不清晰（Mock 结果）',
    details: [
      { phoneme: 'æ', correct: false }
    ]
  }));
});

// 9. 获取报告列表
app.get('/api/v1/reports', (req, res) => {
  res.json(success([
    { reportId: 'rep001', date: '2026-04-16', summary: '发音准确率提升' },
    { reportId: 'rep002', date: '2026-04-15', summary: '今日练习完成度良好' }
  ]));
});

// 10. 获取报告详情
app.get('/api/v1/reports/:reportId', (req, res) => {
  res.json(success({
    reportId: req.params.reportId,
    childId: 'c001',
    totalScore: 85,
    progress: '上升',
    weakPhonemes: ['æ', 'θ'],
    suggestion: '加强 /æ/ 和 /θ/ 的练习'
  }));
});

// 404 处理
app.use((req, res) => {
  res.status(404).json(error(1004, '接口不存在'));
});

// ---------- 启动服务器 ----------
const PORT = 3000;
app.listen(PORT, '0.0.0.0', () => {
  console.log(`🚀 简陋服务器已启动，监听端口 ${PORT}`);
  console.log(`   局域网内可通过本机 IP 访问，例如：http://192.168.x.x:${PORT}`);
});
