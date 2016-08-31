package com.favorites.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.favorites.domain.CollectRepository;
import com.favorites.domain.CollectSummary;
import com.favorites.domain.Favorites;
import com.favorites.domain.FavoritesRepository;
import com.favorites.domain.FollowRepository;
import com.favorites.domain.User;
import com.favorites.domain.UserRepository;
import com.favorites.service.CollectService;

@Controller
@RequestMapping("/")
public class HomeController extends BaseController{
	
	@Autowired
	private CollectService collectService;
	@Autowired
	private FavoritesRepository favoritesRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private CollectRepository collectRepository;
	@Autowired
	private FollowRepository followRepository;
	@Value("${dfs.url}")
	private String dfsUrl;
	
	@RequestMapping(value="/standard/{type}")
	public String standard(Model model,@RequestParam(value = "page", defaultValue = "0") Integer page,
	        @RequestParam(value = "size", defaultValue = "15") Integer size,@PathVariable("type") String type) {
		Sort sort = new Sort(Direction.DESC, "id");
	    Pageable pageable = new PageRequest(page, size, sort);
	    List<CollectSummary> collects=collectService.getCollects(type,getUserId(), pageable,null);
		model.addAttribute("collects", collects);
		model.addAttribute("type", type);
		Favorites favorites = new Favorites();
		if(!"my".equals(type)&&!"explore".equals(type)){
			try {
				favorites = favoritesRepository.findOne(Long.parseLong(type));
			} catch (Exception e) {
				logger.error("获取收藏夹异常：",e);
			}
		}
		model.addAttribute("favorites", favorites);
		model.addAttribute("userId", getUserId());
		model.addAttribute("size", collects.size());
		logger.info("standard end :"+ getUserId());
		return "collect/standard";
	}
	
	
	@RequestMapping(value="/simple/{type}")
	public String simple(Model model,@RequestParam(value = "page", defaultValue = "0") Integer page,
	        @RequestParam(value = "size", defaultValue = "20") Integer size,@PathVariable("type") String type) {
		Sort sort = new Sort(Direction.DESC, "id");
	    Pageable pageable = new PageRequest(page, size, sort);
	    List<CollectSummary> collects=collectService.getCollects(type,getUserId(), pageable,null);
		model.addAttribute("collects", collects);
		model.addAttribute("type", type);
		Favorites favorites = new Favorites();
		if(!"my".equals(type)&&!"explore".equals(type)){
			try {
				favorites = favoritesRepository.findOne(Long.parseLong(type));
			} catch (Exception e) {
				logger.error("获取收藏夹异常：",e);
			}
		}
		model.addAttribute("favorites", favorites);
		model.addAttribute("userId", getUserId());
		model.addAttribute("size", collects.size());
		logger.info("simple end :"+ getUserId());
		return "collect/simple";
	}
	
	/**
	 * 个人首页
	 * @param model
	 * @param userId
	 * @param page
	 * @param size
	 * @return
	 */
	@RequestMapping(value="/user/{userId}/{favoritesId}")
	public String userPageShow(Model model,@PathVariable("userId") Long userId,@PathVariable("favoritesId") Long favoritesId,@RequestParam(value = "page", defaultValue = "0") Integer page,
	        @RequestParam(value = "size", defaultValue = "15") Integer size){
		logger.info("userId:" + userId);
		User user = userRepository.findOne(userId);
		Long collectCount = 0l;
		Sort sort = new Sort(Direction.DESC, "id");
	    Pageable pageable = new PageRequest(page, size, sort);
	    List<CollectSummary> collects = null;
	    Integer isFollow = 0;
		if(getUserId().longValue() == userId.longValue()){
			model.addAttribute("myself","yes");
			collectCount = collectRepository.countByUserId(userId);
			if(0 == favoritesId){
				collects =collectService.getCollects("myself", userId, pageable,null);
			}else{
				collects =collectService.getCollects(String.valueOf(favoritesId), userId, pageable,0l);
			}
		}else{
			model.addAttribute("myself","no");
			collectCount = collectRepository.countByUserIdAndType(userId, "public");
			if(favoritesId == 0){
				collects =collectService.getCollects("others", userId, pageable,null);
			}else{
				collects = collectService.getCollects("otherpublic", userId, pageable, favoritesId);
			}
			isFollow = followRepository.countByUserIdAndFollowIdAndStatus(getUserId(), userId, "follow");
		}
		Integer follow = followRepository.countByUserIdAndStatus(userId, "follow");
		Integer followed = followRepository.countByFollowIdAndStatus(userId, "follow");
		List<Favorites> favoritesList = favoritesRepository.findByUserId(userId);
		List<String> followUser = followRepository.findFollowUserByUserId(userId);
		List<String> followedUser = followRepository.findFollowedUserByFollowId(userId);
		model.addAttribute("collectCount",collectCount);
		model.addAttribute("follow",follow);
		model.addAttribute("followed",followed);
		model.addAttribute("user",user);
		model.addAttribute("collects", collects);
		model.addAttribute("favoritesList",favoritesList);
		model.addAttribute("followUser",followUser);
		model.addAttribute("followedUser",followedUser);
		model.addAttribute("isFollow",isFollow);
		model.addAttribute("dfsUrl",dfsUrl);
		return "user";
	}
	
	
		/**
		 * 个人首页内容替换
		 * @param model
		 * @param userId
		 * @param page
		 * @param size
		 * @return
		 */
		@RequestMapping(value="/usercontent/{userId}/{favoritesId}")
		public String userContentShow(Model model,@PathVariable("userId") Long userId,@PathVariable("favoritesId") Long favoritesId,@RequestParam(value = "page", defaultValue = "0") Integer page,
		        @RequestParam(value = "size", defaultValue = "15") Integer size){
			logger.info("userId:" + userId);
			User user = userRepository.findOne(userId);
			Long collectCount = 0l;
			Sort sort = new Sort(Direction.DESC, "id");
		    Pageable pageable = new PageRequest(page, size, sort);
		    List<CollectSummary> collects = null;
			if(getUserId().longValue() == userId.longValue()){
				model.addAttribute("myself","yes");
				collectCount = collectRepository.countByUserId(userId);
				if(0 == favoritesId){
					collects =collectService.getCollects("myself", userId, pageable,null);
				}else{
					collects =collectService.getCollects(String.valueOf(favoritesId), userId, pageable,0l);
				}
			}else{
				model.addAttribute("myself","no");
				collectCount = collectRepository.countByUserIdAndType(userId, "public");
				if(favoritesId == 0){
					collects =collectService.getCollects("others", userId, pageable,null);
				}else{
					collects = collectService.getCollects("otherpublic", userId, pageable, favoritesId);
				}
			}
			List<Favorites> favoritesList = favoritesRepository.findByUserId(userId);
			model.addAttribute("collectCount",collectCount);
			model.addAttribute("user",user);
			model.addAttribute("collects", collects);
			model.addAttribute("favoritesList",favoritesList);
			model.addAttribute("dfsUrl",dfsUrl);
			return "fragments/usercontent";
		}
	
	
	/**
	 * 搜索
	 * @author neo
	 * @date 2016年8月25日
	 * @param model
	 * @param page
	 * @param size
	 * @param key
	 * @return
	 */
	@RequestMapping(value="/search/{key}")
	public String search(Model model,@RequestParam(value = "page", defaultValue = "0") Integer page,
	        @RequestParam(value = "size", defaultValue = "20") Integer size,@PathVariable("key") String key) {
		Sort sort = new Sort(Direction.DESC, "id");
	    Pageable pageable = new PageRequest(page, size, sort);
	    List<CollectSummary> myCollects=collectService.searchMy(getUserId(),key ,pageable);
	    List<CollectSummary> otherCollects=collectService.searchOther(getUserId(), key, pageable);
		model.addAttribute("myCollects", myCollects);
		model.addAttribute("otherCollects", otherCollects);
		model.addAttribute("userId", getUserId());
		logger.info("search end :"+ getUserId());
		return "collect/search";
	}
	
	
}