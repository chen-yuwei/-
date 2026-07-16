import express from 'express';
import cors from 'cors';
import path from 'path';
import { fileURLToPath } from 'url';
import config from './config.js';
import routes from './routes/index.js';
import { errorHandler } from './middleware/errorHandler.js';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const app = express();

app.use(cors());
app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true }));

app.use(config.file.accessUrl, express.static(path.resolve(config.file.uploadDir)));

app.use('/api', routes);

app.use(errorHandler);

app.listen(config.port, () => {
  console.log(`Reading backend running at http://localhost:${config.port}`);
});
