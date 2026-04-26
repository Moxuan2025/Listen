// 添加错误验证行
// throw new Error("OK！");
require('dotenv').config();
const tencentcloud = require('tencentcloud-sdk-nodejs');

// 初始化 TTS 客户端
const TtsClient = tencentcloud.tts.v20190823.Client;
const ttsClient = new TtsClient({
  credential: {
    secretId: process.env.TENCENT_SECRET_ID,
    secretKey: process.env.TENCENT_SECRET_KEY,
  },
  region: "ap-guangzhou",
  profile: { httpProfile: { endpoint: "tts.tencentcloudapi.com" } }
});

// 初始化口语评测客户端
const SoeClient = tencentcloud.soe.v20180724.Client;
const soeClient = new SoeClient({
  credential: {
    secretId: process.env.TENCENT_SECRET_ID,
    secretKey: process.env.TENCENT_SECRET_KEY,
  },
  region: "ap-guangzhou",
  profile: { httpProfile: { endpoint: "soe.tencentcloudapi.com" } }
});
const fs = require("fs");
const path = require("path");
const express = require("express");
const cors = require("cors");
const multer = require("multer");
const audioStorage = multer.diskStorage({
  destination: (req, file, cb) => {
    const dir = path.join(__dirname, 'data', 'audio');
    if (!fs.existsSync(dir)) fs.mkdirSync(dir, { recursive: true });
    cb(null, dir);
  },
  filename: (req, file, cb) => {
    const ext = path.extname(file.originalname);
    const name = `${Date.now()}-${Math.random().toString(36).substring(2, 8)}${ext}`;
    cb(null, name);
  }
});
const upload = multer({ storage: audioStorage });
const { randomUUID } = require("crypto");

const app = express();
//const upload = multer();

app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));
app.use('/audio', express.static(path.join(__dirname, 'data', 'audio')));

const PORT = process.env.PORT || 8000;

function nowIso() {
  return new Date().toISOString();
}

function newId() {
  return randomUUID().replace(/-/g, "");
}

function apiOk(data = null, message = "ok") {
  return { ok: true, message, data };
}

function apiFail(message, code = 400) {
  const err = new Error(message);
  err.statusCode = code;
  throw err;
}

// ---------- 持久化存储基类 ----------
class MemoryTable {
  constructor() {
    this.rows = new Map();
  }

  create(payload) {
    const id = newId();
    const row = {
      id,
      ...payload,
      created_at: nowIso(),
      updated_at: nowIso(),
    };
    this.rows.set(id, row);
    return row;
  }

  get(id) {
    return this.rows.get(id) || null;
  }

  list() {
    return Array.from(this.rows.values());
  }

  update(id, payload) {
    const row = this.rows.get(id);
    if (!row) return null;
    Object.keys(payload).forEach((k) => {
      if (payload[k] !== undefined) row[k] = payload[k];
    });
    row.updated_at = nowIso();
    this.rows.set(id, row);
    return row;
  }

  delete(id) {
    return this.rows.delete(id);
  }

  findOne(predicate) {
    return this.list().find(predicate) || null;
  }

  findByField(field, value) {
    return this.list().filter(item => item[field] === value);
  }
}

const DATA_DIR = path.join(__dirname, "data");

function ensureDataDir() {
  if (!fs.existsSync(DATA_DIR)) {
    fs.mkdirSync(DATA_DIR, { recursive: true });
  }
}

class JsonTable extends MemoryTable {
  constructor(filename, seedPayloads = []) {
    super();
    ensureDataDir();
    this.filePath = path.join(DATA_DIR, filename);
    this.load(seedPayloads);
  }

  load(seedPayloads = []) {
    if (!fs.existsSync(this.filePath)) {
      console.log(`[JsonTable] 文件不存在，准备创建: ${this.filePath}`);
      const seedRows = seedPayloads.map((payload) => ({
        id: newId(),
        ...payload,
        created_at: nowIso(),
        updated_at: nowIso(),
      }));
      try {
        fs.writeFileSync(this.filePath, JSON.stringify(seedRows, null, 2), "utf8");
        console.log(`[JsonTable] 文件写入成功: ${this.filePath}，记录数: ${seedRows.length}`);
      } catch (err) {
        console.error(`[JsonTable] 文件写入失败: ${this.filePath}`, err);
        throw err;
      }
    } else {
      console.log(`[JsonTable] 文件已存在: ${this.filePath}`);
    }

    try {
      const raw = fs.readFileSync(this.filePath, "utf8");
      const rows = raw.trim() ? JSON.parse(raw) : [];
      this.rows = new Map(rows.map((row) => [row.id, row]));
      console.log(`[JsonTable] 文件加载成功，内存记录数: ${this.rows.size}`);
    } catch (e) {
      console.error(`[JsonTable] 文件读取或解析失败: ${this.filePath}`, e);
      this.rows = new Map();
      fs.writeFileSync(this.filePath, "[]", "utf8");
      console.log(`[JsonTable] 已重置为空文件: ${this.filePath}`);
    }
  }

  save() {
    const rows = Array.from(this.rows.values());
    fs.writeFileSync(this.filePath, JSON.stringify(rows, null, 2), "utf8");
  }

  create(payload) {
    const row = super.create(payload);
    this.save();
    return row;
  }

  update(id, payload) {
    const row = super.update(id, payload);
    if (row) this.save();
    return row;
  }

  delete(id) {
    const ok = super.delete(id);
    if (ok) this.save();
    return ok;
  }
}

// ---------- 数据库实例 ----------
// 用户表：只存储 username, password, role, children(监护人专用), guardian(孩子专用)
// 其他字段一律不要，但内部保留 id, created_at, updated_at 用于管理（不暴露给前端）
const db = {
  users: new JsonTable("users.json", [
    { username: "admin", password: "admin", role: "admin", children: [] },
    { username: "guardian1", password: "guardian1", role: "guardian", children: ["child1"] },
 { username: "child1", password: "child1", role: "child", guardians: ["guardian1"] }
  ]),
  plans: new JsonTable("plans.json"),
  sessions: new JsonTable("sessions.json"),
  records: new JsonTable("records.json"),
  evaluations: new JsonTable("evaluations.json"),
  reports: new JsonTable("reports.json"),
  auth_tokens: new JsonTable("auth_tokens.json"),
  config: {
    app_name: "Speech Training Backend",
    version: "0.4.0",
    storage_mode: "file",
    evaluation_enabled: false,
    supported_audio_formats: ["wav", "mp3", "m4a", "aac"],
  },
};

// ---------- 辅助函数 ----------
// 对外暴露的用户信息（不包含密码、内部 id、时间戳等）
function sanitizeUser(u) {
  if (!u) return null;
  const result = { username: u.username, role: u.role };
  if (u.role === "guardian") {
    result.children = u.children || [];
  } else if (u.role === "child") {
    result.guardians = u.guardians || [];
  }
  // admin 角色不返回 children 或 guardian
  return result;
}

function findUserByUsername(username) {
  if (!username) return null;
  return db.users.findOne((u) => u.username === username);
}

function createTokenForUser(userId) {
  const token = randomUUID().replace(/-/g, "");
  db.auth_tokens.create({ token, user_id: userId });
  return token;
}

function getTokenFromReq(req) {
  const auth = req.headers.authorization || "";
  console.log(`DEBUG Server: Received Authorization Header: ${auth.substring(0, 15)}...`);
  if (auth.startsWith("Bearer ")) return auth.slice(7).trim();
  return req.query.token || req.body.token || "";
}

function authMe(token) {
  if (!token) apiFail("缺少 token", 401);
  const tokenRow = db.auth_tokens.findOne((t) => t.token === token);
  if (!tokenRow) apiFail("无效 token", 401);
  const u = db.users.get(tokenRow.user_id);
  if (!u) apiFail("用户不存在", 404);
  return sanitizeUser(u);
}

function getCurrentUser(token) {
  console.log(`DEBUG Server: Looking up user for token: ${token ? token.substring(0, 10) + '...' : 'EMPTY'}`);
  const tokenRow = db.auth_tokens.findOne((t) => t.token === token);
  if (!tokenRow) {
    console.log(`DEBUG Server: Token not found in database.`);
    return null;
  }
  const user = db.users.get(tokenRow.user_id);
  console.log(`DEBUG Server: Found user: ${user ? user.username : 'NULL'}`);
  return user;
}

function registerUser({ username, password, name = "", role = "guardian", guardian_username = null, children_usernames = [] }) {
  if (!username) apiFail("用户名不能为空", 400);
  if (!password) apiFail("密码不能为空", 400);
  if (!["guardian", "child", "admin"].includes(role)) apiFail("角色必须是 guardian、child 或 admin", 400);

  const exists = findUserByUsername(username);
  if (exists) apiFail("用户名已存在", 409);

  // 孩子注册：监护人可选
  if (role === "child") {
    if (guardian_username) {
      const guardian = findUserByUsername(guardian_username);
      if (!guardian) apiFail("监护人不存在", 404);
      if (guardian.role !== "guardian") apiFail("指定的用户不是监护人", 400);
    }
  }

  // 监护人注册：必须至少关联一个孩子
  if (role === "guardian") {
    if (!children_usernames || !Array.isArray(children_usernames) || children_usernames.length === 0) {
      apiFail("监护人注册必须至少关联一个孩子", 400);
    }
    for (const childName of children_usernames) {
      const child = findUserByUsername(childName);
      if (!child) apiFail(`孩子 ${childName} 不存在`, 404);
      if (child.role !== "child") apiFail(`${childName} 不是孩子角色`, 400);
     // if (child.guardian) apiFail(`孩子 ${childName} 已有监护人`, 409);
    }
  }

  // 构建用户数据
  const userPayload = {
    username,
    password,
    name: name || username,
    role,
  };
  if (role === "child") {
    //userPayload.guardian = guardian_username || null;
    userPayload.guardians = guardian_username ? [guardian_username] : [];
  }
  if (role === "guardian") {
    userPayload.children = children_usernames;
  }

  const user = db.users.create(userPayload);

  // 孩子关联监护人
  if (role === "child" && guardian_username) {
    const guardian = findUserByUsername(guardian_username);
    if (guardian) {
      const updatedChildren = [...(guardian.children || []), username];
      db.users.update(guardian.id, { children: updatedChildren });
    }
  }

  // 监护人关联孩子
  if (role === "guardian") {
    for (const childName of children_usernames) {
      const child = findUserByUsername(childName);
      if (child) {
        //db.users.update(child.id, { guardian: username });
        const updatedGuardians = [...(child.guardians || []), username];
        db.users.update(child.id, { guardians: updatedGuardians });
      }
    }
  }

  // 如果是孩子，自动创建空白档案
  if (role === 'child') {
    try {
      const childDir = path.join(DATA_DIR, 'childfile');
      if (!fs.existsSync(childDir)) fs.mkdirSync(childDir, { recursive: true });
      const profilePath = path.join(childDir, `${username}.json`);
      const profile = {
        name: username,
        hearing_loss_level: "未知",
        listening_scores: [],
        expression_scores: [],
        comprehension_scores: [],
        overall_scores: [],
        current_plan: null,
        latest_report: null
      };
      fs.writeFileSync(profilePath, JSON.stringify(profile, null, 2), 'utf8');
    } catch (e) {
      console.error(`创建孩子档案失败: ${username}`, e);
    }
  }

  const token = createTokenForUser(user.id);
  return { token, user: sanitizeUser(user) };
}

/*// ---------- 注册与登录（）----------
function registerUser({ username, password, name = "", role = "guardian", guardian_username = null }) {
  if (!username) apiFail("用户名不能为空", 400);
  if (!password) apiFail("密码不能为空", 400);
  if (!["guardian", "child", "admin"].includes(role)) apiFail("角色必须是 guardian、child 或 admin", 400);

  const exists = findUserByUsername(username);
  if (exists) apiFail("用户名已存在", 409);

  // 孩子角色：guardian_username 可选，如果提供了则校验存在性
  if (role === "child" && guardian_username) {
    const guardian = findUserByUsername(guardian_username);
    if (!guardian) apiFail("监护人不存在", 404);
    if (guardian.role !== "guardian") apiFail("指定的用户不是监护人", 400);
  }
  // 如果是孩子，自动创建空白档案
if (role === 'child') {
  try {
    const childDir = path.join(DATA_DIR, 'childfile');
    if (!fs.existsSync(childDir)) fs.mkdirSync(childDir, { recursive: true });
    const profilePath = path.join(childDir, `${username}.json`);
    const profile = {
      name: username,
      hearing_loss_level: "未知",
      listening_scores: [],
      expression_scores: [],
      comprehension_scores: [],
      overall_scores: [],
      current_plan: null,
      latest_report: null
    };
    fs.writeFileSync(profilePath, JSON.stringify(profile, null, 2), 'utf8');
  } catch (e) {
    // 不影响注册流程，仅打印错误
    console.error(`创建孩子档案失败: ${username}`, e);
  }
}

  // 创建用户
  const user = db.users.create({
    username,
    password,
    name: name || username,
    role,
    ...(role === "child" && guardian_username ? { guardian: guardian_username } : {}),
    ...(role === "guardian" ? { children: [] } : {})
  });

  // 如果是孩子且提供了监护人，自动更新监护人的 children 列表
  if (role === "child" && guardian_username) {
    const guardian = findUserByUsername(guardian_username);
    if (guardian) {
      const updatedChildren = [...(guardian.children || []), username];
      db.users.update(guardian.id, { children: updatedChildren });
    }
  }

  const token = createTokenForUser(user.id);
  return { token, user: sanitizeUser(user) };
}
*/
function login(username, password) {
  const u = findUserByUsername(username);
  if (!u || u.password !== password) {
    apiFail("用户名或密码错误", 401);
  }
  const token = createTokenForUser(u.id);
  return { token, user: sanitizeUser(u) };
}

// ---------- 业务辅助：获取监护人可见的孩子列表 ----------
function getGuardianChildren(guardianUsername) {
  const guardian = findUserByUsername(guardianUsername);
  if (!guardian || guardian.role !== "guardian") return [];
  return guardian.children || [];
}

// ---------- CRUD 包装（用于非用户表）----------
function crud(table) {
  return {
    list: () => table.list(),
    get: (id) => {
      const row = table.get(id);
      if (!row) apiFail("记录不存在", 404);
      return row;
    },
    create: (payload) => table.create(payload),
    update: (id, payload) => {
      const row = table.update(id, payload);
      if (!row) apiFail("记录不存在", 404);
      return row;
    },
    delete: (id) => {
      const ok = table.delete(id);
      if (!ok) apiFail("记录不存在", 404);
      return { deleted: true, id };
    },
  };
}

const plansService = crud(db.plans);
const sessionsService = crud(db.sessions);
const recordsService = crud(db.records);
const evaluationsService = crud(db.evaluations);
const reportsService = crud(db.reports);

// ---------- Bootstrap 数据（按角色过滤）----------
function bootstrapPayload(token) {
  let user = null;
  let currentFullUser = null;
  try {
    user = authMe(token);
    currentFullUser = getCurrentUser(token);
  } catch {
    user = null;
  }

  let visibleChildren = [];
  if (currentFullUser) {
    if (currentFullUser.role === "guardian") {
      visibleChildren = getGuardianChildren(currentFullUser.username);
    } else if (currentFullUser.role === "admin") {
      // admin 可以看到所有 child 角色的用户
      visibleChildren = db.users.findByField("role", "child").map(c => c.username);
    } else if (currentFullUser.role === "child") {
      visibleChildren = [currentFullUser.username];
    }
  }

  return {
    user,
    children: visibleChildren,
    plans: db.plans.list(),
    sessions: db.sessions.list(),
    records: db.records.list(),
    evaluations: db.evaluations.list(),
    reports: db.reports.list(),
    config: db.config,
  };
}

// ---------- Express 路由 ----------
app.get("/health", (req, res) => {
  res.json(apiOk({ status: "healthy", time: nowIso() }));
});

app.get("/api/v1/bootstrap", (req, res) => {
  const token = getTokenFromReq(req);
  res.json(apiOk(bootstrapPayload(token)));
});

app.get("/api/v1/config", (req, res) => {
  res.json(apiOk(db.config));
});

// 检查用户名是否存在
app.get("/api/v1/auth/check-username", (req, res) => {
  const username = (req.query.username || "").trim();
  const exists = !!findUserByUsername(username);
  res.json(apiOk({ exists }));
});

// POST /api/v1/tts/synthesize
app.post('/api/v1/tts/synthesize', async (req, res) => {
  try {
    const { text, voiceType = 1002, speed = 0, volume = 0 } = req.body;
    
    // 文本长度限制：中文最多 600 字，英文最多 1800 字母
    if (!text || text.length > 600) {
      return res.status(400).json(apiFail("文本不能为空且不能超过600字"));
    }

    const params = {
      Text: text,
      SessionId: newId(),
      VoiceType: voiceType,    // 可选声音类型，1002 为智瑜（女声）
      Speed: speed,            // -2 到 6，0 为正常语速
      Volume: volume,          // -10 到 10，0 为正常音量
      Codec: "mp3",            // 返回格式 mp3/pcm
    };
    
    const response = await ttsClient.TextToVoice(params);
    // 返回 base64 编码的音频内容
    res.json(apiOk({ 
      audio: response.Audio,   // base64 编码的音频数据
      sessionId: response.SessionId
    }));
  } catch (error) {
    console.error("TTS 合成失败:", error);
    res.status(500).json(apiFail(error.message || "语音合成失败"));
  }
});

// POST /api/v1/soe/init
app.post('/api/v1/soe/init', async (req, res) => {
  try {
    const { refText, evalMode = "Word", scoreCoeff = 1.0 } = req.body;
    
    const sessionId = newId();
    const params = {
      SessionId: sessionId,
      RefText: refText,                       // 标准文本
      EvalMode: evalMode,                     // Word/Sentence/Paragraph
      ScoreCoeff: scoreCoeff,                 // 评分数值系数
    };
    
    // 调用初始化接口
    await soeClient.InitOralProcess(params);
    res.json(apiOk({ sessionId, refText }));
  } catch (error) {
    console.error("初始化评测失败:", error);
    res.status(500).json(apiFail(error.message || "初始化失败"));
  }
});

// POST /api/v1/soe/evaluate
app.post('/api/v1/soe/evaluate', async (req, res) => {
  try {
    const { 
      sessionId, 
      seqId = 1,           // 音频分片序号
      isEnd = 1,           // 是否最后一个分片（1是0否）
      userVoiceData,       // base64 编码的语音数据
      voiceEncodeType = 1, // 1:pcm / 2:wav / 3:mp3
      voiceFileType = 3    // 同上
    } = req.body;
    
    const params = {
      SessionId: sessionId,
      SeqId: seqId,
      IsEnd: isEnd,
      UserVoiceData: userVoiceData,
      VoiceEncodeType: voiceEncodeType,
      VoiceFileType: voiceFileType,
    };
    
    const response = await soeClient.OralProcess(params);
    res.json(apiOk({
      pronAccuracy: response.PronAccuracy,      // 发音准确度
      pronFluency: response.PronFluency,        // 流利度
      pronCompletion: response.PronCompletion,  // 完整度
      suggestedScore: response.SuggestedScore,  // 推荐分数
    }));
  } catch (error) {
    console.error("口语评测失败:", error);
    res.status(500).json(apiFail(error.message || "评测失败"));
  }
});
// 注册
app.post("/api/v1/auth/register", (req, res) => {
  const { username, password, role, guardian, children } = req.body;
  const result = registerUser({
    username: (username || "").trim(),
    password: password || "",
    role: role || "guardian",
    guardian_username: guardian ? guardian.trim() : null,
    children_usernames: children || [],
  });
  res.json(apiOk(result, "注册成功"));
});

/*app.post("/api/v1/auth/register", (req, res) => {
  const { username, password, role, guardian, children } = req.body;
  const newUser = registerUser({
    username: (username || "").trim(),
    password: password || "",
    role: role || "guardian",
    guardian: guardian ? guardian.trim() : null,
    children: children || [],
  });
  const token = createTokenForUser(newUser.id);
  res.json(apiOk({ token, user: sanitizeUser(newUser) }, "注册成功"));
});*/

// 登录
app.post("/api/v1/auth/login", (req, res) => {
  const { username, password } = req.body;
  const result = login((username || "").trim(), password || "");
  res.json(apiOk(result, "登录成功"));
});

// 获取当前用户信息
app.get("/api/v1/auth/me", (req, res) => {
  const token = getTokenFromReq(req);
  res.json(apiOk(authMe(token)));
});

// 登出
app.post("/api/v1/auth/logout", (req, res) => {
  const token = getTokenFromReq(req);
  const tokenRow = db.auth_tokens.findOne((t) => t.token === token);
  if (tokenRow) {
    db.auth_tokens.delete(tokenRow.id);
  }
  res.json(apiOk({ logout: true }));
});

// ---------- 孩子管理（基于用户名，无需额外 ID）----------
// 获取当前用户可见的孩子列表
app.get("/api/v1/children", (req, res) => {
  console.log("DEBUG /children called");
  const token = getTokenFromReq(req);
  const currentUser = getCurrentUser(token);
  if (!currentUser) apiFail("未登录", 401);
  let childrenList = [];
  if (currentUser.role === "guardian") {
    const usernames = getGuardianChildren(currentUser.username);
    // 转换为包含详细信息的对象
    childrenList = usernames.map(username => {
      const child = findUserByUsername(username);
      return {
        username: username,
        name: child ? (child.name || username) : username
      };
    });
  } else if (currentUser.role === "admin") {
    childrenList = db.users.findByField("role", "child").map(c => ({
      username: c.username,
      name: c.name || c.username
    }));
  } else if (currentUser.role === "child") {
    childrenList = [{ 
      username: currentUser.username, 
      name: currentUser.name || currentUser.username 
    }];
  }
  res.json(apiOk(childrenList));
});

// 监护人添加一个已有孩子（关联）
app.post("/api/v1/children/add", (req, res) => {
  const token = getTokenFromReq(req);
  const currentUser = getCurrentUser(token);
  if (!currentUser) apiFail("未登录", 401);
  if (currentUser.role !== "guardian") apiFail("只有监护人可添加孩子", 403);
  const { childUsername } = req.body;
  if (!childUsername) apiFail("孩子用户名不能为空", 400);
  const childUser = findUserByUsername(childUsername);
  if (!childUser || childUser.role !== "child") apiFail("孩子不存在或角色不是 child", 404);
  // 检查是否已经关联
  if (currentUser.children.includes(childUsername)) apiFail("孩子已关联", 409);
  // 更新监护人的 children 列表
  const newChildren = [...currentUser.children, childUsername];
  db.users.update(currentUser.id, { children: newChildren });
const currentGuardians = childUser.guardians || [];
if (!currentGuardians.includes(currentUser.username)) {
  const updatedGuardians = [...currentGuardians, currentUser.username];
  db.users.update(childUser.id, { guardians: updatedGuardians });
}
  res.json(apiOk({ childUsername, guardian: currentUser.username }, "添加成功"));
});

// 监护人移除孩子（解除关联）
app.post("/api/v1/children/remove", (req, res) => {
  const token = getTokenFromReq(req);
  const currentUser = getCurrentUser(token);
  if (!currentUser) apiFail("未登录", 401);
  if (currentUser.role !== "guardian") apiFail("只有监护人可移除孩子", 403);
  const { childUsername } = req.body;
  if (!childUsername) apiFail("孩子用户名不能为空", 400);
  if (!currentUser.children.includes(childUsername)) apiFail("该孩子未关联", 404);
  // 更新监护人 children 列表
  const newChildren = currentUser.children.filter(c => c !== childUsername);
  db.users.update(currentUser.id, { children: newChildren });
  // 更新孩子的 guardian 字段为 null（或空）
  const childUser = findUserByUsername(childUsername);
  /*if (childUser && childUser.guardian === currentUser.username) {
    db.users.update(childUser.id, { guardian: null });
  }*/
  if (childUser && childUser.guardians && childUser.guardians.includes(currentUser.username)) {
  const updatedGuardians = childUser.guardians.filter(g => g !== currentUser.username);
  db.users.update(childUser.id, { guardians: updatedGuardians });
}
  res.json(apiOk({ childUsername, removed: true }, "移除成功"));
});

// 获取某个孩子的详细信息（仅监护人、admin或孩子本人）
app.get("/api/v1/children/:username", (req, res) => {
  const token = getTokenFromReq(req);
  const currentUser = getCurrentUser(token);
  if (!currentUser) apiFail("未登录", 401);
  const childUsername = req.params.username;
  const childUser = findUserByUsername(childUsername);
  if (!childUser || childUser.role !== "child") apiFail("孩子不存在", 404);
  // 权限检查
  if (currentUser.role !== "admin" && 
      currentUser.role !== "child" && 
      !(currentUser.role === "guardian" && currentUser.children.includes(childUsername))) {
    apiFail("无权查看", 403);
  }
  res.json(apiOk(sanitizeUser(childUser)));
});

// ---------- 计划、会话、记录等业务表（关联孩子使用 child_username）----------
app.get("/api/v1/plans", (req, res) => res.json(apiOk(plansService.list())));
app.post("/api/v1/plans", (req, res) => {
  res.json(apiOk(plansService.create({ ...req.body, status: "draft" })));
});
app.get("/api/v1/plans/:id", (req, res) => res.json(apiOk(plansService.get(req.params.id))));
app.put("/api/v1/plans/:id", (req, res) => res.json(apiOk(plansService.update(req.params.id, req.body))));
app.delete("/api/v1/plans/:id", (req, res) => res.json(apiOk(plansService.delete(req.params.id))));
app.post("/api/v1/plans/:id/generate", (req, res) => {
  const plan = plansService.get(req.params.id);
  const updated = plansService.update(req.params.id, {
    status: "generated",
    goal: plan.goal || "占位目标",
  });
  res.json(apiOk(updated));
});

app.get("/api/v1/sessions", (req, res) => res.json(apiOk(sessionsService.list())));
app.post("/api/v1/sessions/start", (req, res) => {
  const data = {
    ...req.body,
    status: "running",
    started_at: nowIso(),
    finished_at: null,
    summary: null,
    duration_seconds: null,
  };
  res.json(apiOk(sessionsService.create(data)));
});
app.get("/api/v1/sessions/:id", (req, res) => res.json(apiOk(sessionsService.get(req.params.id))));
app.post("/api/v1/sessions/:id/finish", (req, res) => {
  const updated = sessionsService.update(req.params.id, {
    status: "finished",
    finished_at: nowIso(),
    ...req.body,
  });
  res.json(apiOk(updated));
});
app.delete("/api/v1/sessions/:id", (req, res) => res.json(apiOk(sessionsService.delete(req.params.id))));

app.get("/api/v1/records", (req, res) => res.json(apiOk(recordsService.list())));
app.post("/api/v1/records", (req, res) => {
  // 这里 child_id 应改为 child_username，保持一致性
  const data = { ...req.body, transcript: null };
  res.json(apiOk(recordsService.create(data)));
});
app.get("/api/v1/records/:id", (req, res) => res.json(apiOk(recordsService.get(req.params.id))));
app.delete("/api/v1/records/:id", (req, res) => res.json(apiOk(recordsService.delete(req.params.id))));

app.post("/api/v1/records/upload", upload.single("file"), (req, res) => {
  const filename = req.file ? req.file.originalname : "audio.bin";
  const row = recordsService.create({
    child_username: req.body.child_username,
    session_id: req.body.session_id || null,
    target_text: req.body.target_text || null,
    audio_url: `mock://${filename}`,
    transcript: null,
    source: "upload",
  });
  res.json(apiOk(row));
});

app.get("/api/v1/evaluations", (req, res) => res.json(apiOk(evaluationsService.list())));
app.post("/api/v1/evaluations/run", (req, res) => {
  const row = evaluationsService.create({
    record_id: req.body.record_id,
    score: 80,
    pronunciation_score: 78,
    fluency_score: 81,
    volume_score: 79,
    notes: ["当前为占位评分", "后续替换为语音评测模型"],
    details: {
      mode: req.body.mode || "auto",
      aligned: false,
      segments: [],
    },
  });
  res.json(apiOk(row));
});
app.get("/api/v1/evaluations/:id", (req, res) => res.json(apiOk(evaluationsService.get(req.params.id))));

app.get("/api/v1/reports", (req, res) => res.json(apiOk(reportsService.list())));
app.post("/api/v1/reports/generate", (req, res) => {
  const row = reportsService.create({
    child_username: req.body.child_username,
    plan_id: req.body.plan_id || null,
    title: "学习/评测报告",
    content: {
      summary: "占位报告",
      progress: {
        sessions: 0,
        records: 0,
        avg_score: null,
      },
      period: {
        start: req.body.period_start || null,
        end: req.body.period_end || null,
      },
    },
  });
  res.json(apiOk(row));
});
app.get("/api/v1/reports/:id", (req, res) => res.json(apiOk(reportsService.get(req.params.id))));
app.delete("/api/v1/reports/:id", (req, res) => res.json(apiOk(reportsService.delete(req.params.id))));

app.get("/api/v1/sync/bootstrap", (req, res) => {
  const token = getTokenFromReq(req);
  const currentUser = getCurrentUser(token);
  let childrenList = [];
  if (currentUser) {
    if (currentUser.role === "guardian") childrenList = currentUser.children || [];
    else if (currentUser.role === "admin") childrenList = db.users.findByField("role", "child").map(c => c.username);
    else if (currentUser.role === "child") childrenList = [currentUser.username];
  }
  res.json(apiOk({
    children: childrenList,
    plans: db.plans.list(),
    sessions: db.sessions.list(),
    records: db.records.list(),
    evaluations: db.evaluations.list(),
    reports: db.reports.list(),
    config: db.config,
  }));
});

// 获取孩子详细档案
app.get("/api/v1/children/:username/profile", (req, res) => {
  const token = getTokenFromReq(req);
  const currentUser = getCurrentUser(token);
  if (!currentUser) apiFail("未登录", 401);
  
  const childUsername = req.params.username;
  // 权限检查：只有监护人、admin或孩子本人可查看
  if (currentUser.role !== "admin" && 
      currentUser.role !== "child" && 
      !(currentUser.role === "guardian" && currentUser.children.includes(childUsername))) {
    apiFail("无权查看该档案", 403);
  }

  try {
    const profilePath = path.join(DATA_DIR, 'childfile', `${childUsername}.json`);
    if (!fs.existsSync(profilePath)) {
      // 如果档案不存在，返回一个默认空档案
      return res.json(apiOk({
        name: childUsername,
        hearing_loss_level: "未知",
        listening_scores: [],
        expression_scores: [],
        comprehension_scores: [],
        overall_scores: [],
        current_plan: null,
        latest_report: null
      }));
    }
    const raw = fs.readFileSync(profilePath, "utf8");
    const profile = JSON.parse(raw);
    res.json(apiOk(profile));
  } catch (e) {
    console.error("读取档案失败:", e);
    res.status(500).json(apiFail("读取档案失败"));
  }
});

// 全局错误处理
app.use((err, req, res, next) => {
  const status = err.statusCode || 500;
  res.status(status).json({
    ok: false,
    message: err.message || "server error",
    data: null,
  });
});

app.listen(PORT, "0.0.0.0", () => {
  console.log(`Server running at http://0.0.0.0:${PORT}`);
});
