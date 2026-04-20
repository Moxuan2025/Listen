const express = require("express");
const cors = require("cors");
const multer = require("multer");
const { randomUUID } = require("crypto");

const app = express();
const upload = multer();

app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

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
}

const db = {
  users: new MemoryTable(),
  children: new MemoryTable(),
  guardians: new MemoryTable(),
  plans: new MemoryTable(),
  sessions: new MemoryTable(),
  records: new MemoryTable(),
  evaluations: new MemoryTable(),
  reports: new MemoryTable(),
  config: {
    app_name: "Speech Training Backend",
    version: "0.1.0",
    storage_mode: "memory",
    evaluation_enabled: false,
    supported_audio_formats: ["wav", "mp3", "m4a", "aac"],
  },
};

db.users.create({
  username: "admin",
  password: "admin",
  role: "admin",
  name: "系统管理员",
});

function getFirstUser() {
  return db.users.list()[0] || null;
}

function authMe(token) {
  if (token !== "mock-token") apiFail("无效 token", 401);
  const u = getFirstUser();
  if (!u) apiFail("用户不存在", 404);
  return {
    id: u.id,
    username: u.username,
    role: u.role,
    name: u.name,
    created_at: u.created_at,
  };
}

function login(username, password) {
  const u = db.users.list().find((x) => x.username === username && x.password === password);
  if (!u) apiFail("用户名或密码错误", 401);
  return {
    token: "mock-token",
    user_id: u.id,
    role: u.role,
    name: u.name,
  };
}

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

const childrenService = crud(db.children);
const guardiansService = crud(db.guardians);
const plansService = crud(db.plans);
const sessionsService = crud(db.sessions);
const recordsService = crud(db.records);
const evaluationsService = crud(db.evaluations);
const reportsService = crud(db.reports);

function bootstrapPayload(token = "mock-token") {
  let user = null;
  try {
    user = authMe(token);
  } catch {
    user = null;
  }

  return {
    user,
    children: db.children.list(),
    guardians: db.guardians.list(),
    plans: db.plans.list(),
    sessions: db.sessions.list(),
    records: db.records.list(),
    evaluations: db.evaluations.list(),
    reports: db.reports.list(),
    config: db.config,
  };
}

app.get("/health", (req, res) => {
  res.json(apiOk({ status: "healthy", time: nowIso() }));
});

app.get("/api/v1/bootstrap", (req, res) => {
  res.json(apiOk(bootstrapPayload(req.query.token || "mock-token")));
});

app.get("/api/v1/config", (req, res) => {
  res.json(apiOk(db.config));
});

app.post("/api/v1/auth/login", (req, res) => {
  const { username, password } = req.body;
  res.json(apiOk(login(username, password)));
});

app.get("/api/v1/auth/me", (req, res) => {
  res.json(apiOk(authMe(req.query.token || "mock-token")));
});

app.post("/api/v1/auth/logout", (req, res) => {
  res.json(apiOk({ logout: true }));
});

app.get("/api/v1/children", (req, res) => res.json(apiOk(childrenService.list())));
app.post("/api/v1/children", (req, res) => res.json(apiOk(childrenService.create(req.body))));
app.get("/api/v1/children/:id", (req, res) => res.json(apiOk(childrenService.get(req.params.id))));
app.put("/api/v1/children/:id", (req, res) => res.json(apiOk(childrenService.update(req.params.id, req.body))));
app.delete("/api/v1/children/:id", (req, res) => res.json(apiOk(childrenService.delete(req.params.id))));

app.get("/api/v1/guardians", (req, res) => res.json(apiOk(guardiansService.list())));
app.post("/api/v1/guardians", (req, res) => res.json(apiOk(guardiansService.create(req.body))));
app.get("/api/v1/guardians/:id", (req, res) => res.json(apiOk(guardiansService.get(req.params.id))));
app.put("/api/v1/guardians/:id", (req, res) => res.json(apiOk(guardiansService.update(req.params.id, req.body))));
app.delete("/api/v1/guardians/:id", (req, res) => res.json(apiOk(guardiansService.delete(req.params.id))));

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
  const data = { ...req.body, transcript: null };
  res.json(apiOk(recordsService.create(data)));
});
app.get("/api/v1/records/:id", (req, res) => res.json(apiOk(recordsService.get(req.params.id))));
app.delete("/api/v1/records/:id", (req, res) => res.json(apiOk(recordsService.delete(req.params.id))));

app.post("/api/v1/records/upload", upload.single("file"), (req, res) => {
  const filename = req.file ? req.file.originalname : "audio.bin";
  const row = recordsService.create({
    child_id: req.body.child_id,
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
    child_id: req.body.child_id,
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
  res.json(apiOk({
    children: db.children.list(),
    guardians: db.guardians.list(),
    plans: db.plans.list(),
    sessions: db.sessions.list(),
    records: db.records.list(),
    evaluations: db.evaluations.list(),
    reports: db.reports.list(),
    config: db.config,
  }));
});

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
