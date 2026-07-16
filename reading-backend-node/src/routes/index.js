import { Router } from 'express';
import { success } from '../utils/result.js';
import { requireAuth } from '../middleware/auth.js';
import * as authService from '../services/authService.js';
import * as userService from '../services/userService.js';
import * as bookService from '../services/bookService.js';
import * as chapterService from '../services/chapterService.js';
import * as categoryService from '../services/categoryService.js';
import * as bookshelfService from '../services/bookshelfService.js';
import * as readingProgressService from '../services/readingProgressService.js';
import * as readingHistoryService from '../services/readingHistoryService.js';
import * as commentService from '../services/commentService.js';
import * as fileService from '../services/fileService.js';
import * as adminUserService from '../services/adminUserService.js';
import * as adminStatisticsService from '../services/adminStatisticsService.js';
import { optionalAuth, requireAdmin } from '../middleware/auth.js';
import multer from 'multer';

const router = Router();
const upload = multer({ storage: multer.memoryStorage(), limits: { fileSize: 5 * 1024 * 1024 } });

// Auth
router.post('/auth/register', async (req, res, next) => {
  try {
    await authService.register(req.body);
    res.json(success());
  } catch (err) { next(err); }
});

router.post('/auth/login', async (req, res, next) => {
  try {
    res.json(success(await authService.login(req.body)));
  } catch (err) { next(err); }
});

router.get('/auth/current-user', requireAuth, async (req, res, next) => {
  try {
    res.json(success(await authService.getCurrentUser(req.user)));
  } catch (err) { next(err); }
});

// User
router.get('/user/profile', requireAuth, async (req, res, next) => {
  try {
    res.json(success(await userService.getProfile(req.user.id)));
  } catch (err) { next(err); }
});

router.put('/user/profile', requireAuth, async (req, res, next) => {
  try {
    res.json(success(await userService.updateProfile(req.user.id, req.body)));
  } catch (err) { next(err); }
});

router.put('/user/password', requireAuth, async (req, res, next) => {
  try {
    await userService.updatePassword(req.user.id, req.body);
    res.json(success());
  } catch (err) { next(err); }
});

// Books (specific routes before :id)
router.get('/books/recommended', async (req, res, next) => {
  try {
    res.json(success(await bookService.getRecommendedBooks(parseInt(req.query.limit || '8', 10))));
  } catch (err) { next(err); }
});

router.get('/books/hot', async (req, res, next) => {
  try {
    res.json(success(await bookService.getHotBooks(parseInt(req.query.limit || '8', 10))));
  } catch (err) { next(err); }
});

router.get('/books/latest', async (req, res, next) => {
  try {
    res.json(success(await bookService.getLatestBooks(parseInt(req.query.limit || '8', 10))));
  } catch (err) { next(err); }
});

router.get('/books/search', async (req, res, next) => {
  try {
    res.json(success(await bookService.searchBooks(req.query)));
  } catch (err) { next(err); }
});

router.get('/books/:bookId/comments', async (req, res, next) => {
  try {
    const pageNum = parseInt(req.query.pageNum || '1', 10);
    const pageSize = parseInt(req.query.pageSize || '10', 10);
    res.json(success(await commentService.listBookComments(req.params.bookId, pageNum, pageSize)));
  } catch (err) { next(err); }
});

router.get('/books/:bookId/chapters', optionalAuth, async (req, res, next) => {
  try {
    const pageNum = parseInt(req.query.pageNum || '1', 10);
    const pageSize = parseInt(req.query.pageSize || '50', 10);
    res.json(success(await chapterService.listChapters(req.params.bookId, pageNum, pageSize, req.user?.id, true)));
  } catch (err) { next(err); }
});

router.get('/books', async (req, res, next) => {
  try {
    res.json(success(await bookService.listBooks(req.query, true)));
  } catch (err) { next(err); }
});

router.get('/books/:id', optionalAuth, async (req, res, next) => {
  try {
    res.json(success(await bookService.getBookDetail(req.params.id, req.user?.id)));
  } catch (err) { next(err); }
});

// Chapters
router.get('/chapters/:chapterId/previous', async (req, res, next) => {
  try {
    res.json(success(await chapterService.getPreviousChapter(req.params.chapterId, true)));
  } catch (err) { next(err); }
});

router.get('/chapters/:chapterId/next', async (req, res, next) => {
  try {
    res.json(success(await chapterService.getNextChapter(req.params.chapterId, true)));
  } catch (err) { next(err); }
});

router.get('/chapters/:chapterId', optionalAuth, async (req, res, next) => {
  try {
    const clientKey = req.ip;
    res.json(success(await chapterService.getChapterDetail(req.params.chapterId, req.user?.id, clientKey, true)));
  } catch (err) { next(err); }
});

// Categories
router.get('/categories', async (req, res, next) => {
  try {
    res.json(success(await categoryService.listEnabledCategories()));
  } catch (err) { next(err); }
});

router.get('/categories/:categoryId/books', async (req, res, next) => {
  try {
    res.json(success(await bookService.getBooksByCategory(req.params.categoryId, req.query)));
  } catch (err) { next(err); }
});

// Bookshelf
router.get('/bookshelf', requireAuth, async (req, res, next) => {
  try {
    const pageNum = parseInt(req.query.pageNum || '1', 10);
    const pageSize = parseInt(req.query.pageSize || '10', 10);
    res.json(success(await bookshelfService.listBookshelf(req.user.id, pageNum, pageSize)));
  } catch (err) { next(err); }
});

router.post('/bookshelf/:bookId', requireAuth, async (req, res, next) => {
  try {
    await bookshelfService.addToBookshelf(req.user.id, req.params.bookId);
    res.json(success());
  } catch (err) { next(err); }
});

router.delete('/bookshelf/:bookId', requireAuth, async (req, res, next) => {
  try {
    await bookshelfService.removeFromBookshelf(req.user.id, req.params.bookId);
    res.json(success());
  } catch (err) { next(err); }
});

router.put('/bookshelf/:bookId/status', requireAuth, async (req, res, next) => {
  try {
    await bookshelfService.updateReadingStatus(req.user.id, req.params.bookId, parseInt(req.query.readingStatus, 10));
    res.json(success());
  } catch (err) { next(err); }
});

router.get('/bookshelf/check/:bookId', requireAuth, async (req, res, next) => {
  try {
    res.json(success(await bookshelfService.isInBookshelf(req.user.id, req.params.bookId)));
  } catch (err) { next(err); }
});

// Reading progress
router.get('/reading-progress/:bookId', requireAuth, async (req, res, next) => {
  try {
    res.json(success(await readingProgressService.getProgress(req.user.id, req.params.bookId)));
  } catch (err) { next(err); }
});

router.put('/reading-progress', requireAuth, async (req, res, next) => {
  try {
    await readingProgressService.saveOrUpdateProgress(req.user.id, req.body);
    res.json(success());
  } catch (err) { next(err); }
});

// Reading history
router.get('/reading-history', requireAuth, async (req, res, next) => {
  try {
    const pageNum = parseInt(req.query.pageNum || '1', 10);
    const pageSize = parseInt(req.query.pageSize || '10', 10);
    res.json(success(await readingHistoryService.listHistory(req.user.id, pageNum, pageSize)));
  } catch (err) { next(err); }
});

router.post('/reading-history', requireAuth, async (req, res, next) => {
  try {
    await readingHistoryService.addHistory(req.user.id, req.body);
    res.json(success());
  } catch (err) { next(err); }
});

router.delete('/reading-history/:id', requireAuth, async (req, res, next) => {
  try {
    await readingHistoryService.deleteHistory(req.user.id, req.params.id);
    res.json(success());
  } catch (err) { next(err); }
});

router.delete('/reading-history', requireAuth, async (req, res, next) => {
  try {
    await readingHistoryService.clearHistory(req.user.id);
    res.json(success());
  } catch (err) { next(err); }
});

// Comments
router.post('/comments', requireAuth, async (req, res, next) => {
  try {
    await commentService.createComment(req.user.id, req.body);
    res.json(success());
  } catch (err) { next(err); }
});

router.delete('/comments/:id', requireAuth, async (req, res, next) => {
  try {
    await commentService.deleteComment(req.user.id, req.params.id, false);
    res.json(success());
  } catch (err) { next(err); }
});

// Files
router.post('/files/upload', requireAuth, upload.single('file'), async (req, res, next) => {
  try {
    res.json(success(fileService.upload(req.file, req.body.subDir || req.query.subDir || 'common')));
  } catch (err) { next(err); }
});

// Admin routes
const adminRouter = Router();
adminRouter.use(requireAuth, requireAdmin);

adminRouter.get('/statistics', async (req, res, next) => {
  try {
    res.json(success(await adminStatisticsService.getStatistics()));
  } catch (err) { next(err); }
});

adminRouter.get('/users', async (req, res, next) => {
  try {
    res.json(success(await adminUserService.listUsers(req.query)));
  } catch (err) { next(err); }
});

adminRouter.get('/users/:id', async (req, res, next) => {
  try {
    res.json(success(await adminUserService.getUserDetail(req.params.id)));
  } catch (err) { next(err); }
});

adminRouter.put('/users/:id/status', async (req, res, next) => {
  try {
    await adminUserService.updateUserStatus(req.user.id, req.params.id, parseInt(req.query.status, 10));
    res.json(success());
  } catch (err) { next(err); }
});

adminRouter.get('/books', async (req, res, next) => {
  try {
    res.json(success(await bookService.adminListBooks(req.query)));
  } catch (err) { next(err); }
});

adminRouter.get('/books/:id', async (req, res, next) => {
  try {
    res.json(success(await bookService.adminGetBookDetail(req.params.id)));
  } catch (err) { next(err); }
});

adminRouter.post('/books', async (req, res, next) => {
  try {
    res.json(success(await bookService.createBook(req.body)));
  } catch (err) { next(err); }
});

adminRouter.put('/books/:id', async (req, res, next) => {
  try {
    await bookService.updateBook(req.params.id, req.body);
    res.json(success());
  } catch (err) { next(err); }
});

adminRouter.delete('/books/:id', async (req, res, next) => {
  try {
    await bookService.deleteBook(req.params.id);
    res.json(success());
  } catch (err) { next(err); }
});

adminRouter.put('/books/:id/publish-status', async (req, res, next) => {
  try {
    await bookService.updatePublishStatus(req.params.id, parseInt(req.query.publishStatus, 10));
    res.json(success());
  } catch (err) { next(err); }
});

adminRouter.put('/books/:id/recommended', async (req, res, next) => {
  try {
    await bookService.updateRecommended(req.params.id, parseInt(req.query.isRecommended, 10));
    res.json(success());
  } catch (err) { next(err); }
});

adminRouter.get('/books/:bookId/chapters', async (req, res, next) => {
  try {
    const pageNum = parseInt(req.query.pageNum || '1', 10);
    const pageSize = parseInt(req.query.pageSize || '50', 10);
    res.json(success(await chapterService.adminListChapters(req.params.bookId, pageNum, pageSize)));
  } catch (err) { next(err); }
});

adminRouter.post('/chapters', async (req, res, next) => {
  try {
    res.json(success(await chapterService.createChapter(req.body)));
  } catch (err) { next(err); }
});

adminRouter.put('/chapters/:id', async (req, res, next) => {
  try {
    await chapterService.updateChapter(req.params.id, req.body);
    res.json(success());
  } catch (err) { next(err); }
});

adminRouter.delete('/chapters/:id', async (req, res, next) => {
  try {
    await chapterService.deleteChapter(req.params.id);
    res.json(success());
  } catch (err) { next(err); }
});

adminRouter.get('/categories', async (req, res, next) => {
  try {
    res.json(success(await categoryService.listAllCategories()));
  } catch (err) { next(err); }
});

adminRouter.post('/categories', async (req, res, next) => {
  try {
    res.json(success(await categoryService.createCategory(req.body)));
  } catch (err) { next(err); }
});

adminRouter.put('/categories/:id', async (req, res, next) => {
  try {
    await categoryService.updateCategory(req.params.id, req.body);
    res.json(success());
  } catch (err) { next(err); }
});

adminRouter.delete('/categories/:id', async (req, res, next) => {
  try {
    await categoryService.deleteCategory(req.params.id);
    res.json(success());
  } catch (err) { next(err); }
});

adminRouter.get('/comments', async (req, res, next) => {
  try {
    res.json(success(await commentService.adminListComments(req.query)));
  } catch (err) { next(err); }
});

adminRouter.put('/comments/:id/status', async (req, res, next) => {
  try {
    await commentService.updateCommentStatus(req.params.id, parseInt(req.query.status, 10));
    res.json(success());
  } catch (err) { next(err); }
});

adminRouter.delete('/comments/:id', async (req, res, next) => {
  try {
    await commentService.adminDeleteComment(req.params.id);
    res.json(success());
  } catch (err) { next(err); }
});

router.use('/admin', adminRouter);

export default router;
