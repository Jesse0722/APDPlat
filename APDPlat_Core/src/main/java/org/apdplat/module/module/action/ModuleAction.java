/**
 * 
 * APDPlat - Application Product Development Platform
 * Copyright (c) 2013, 杨尚川, yang-shangchuan@qq.com
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package org.apdplat.module.module.action;

import org.apdplat.module.module.model.Module;
import org.apdplat.module.module.service.ModuleService;
import org.apdplat.platform.action.ExtJSSimpleAction;
import javax.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.apdplat.module.module.service.ModuleCache;
import org.apdplat.module.security.service.UserHolder;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ResponseBody;

/**
* 为树形模块导航菜单服务
* @author 杨尚川
*/
@Controller
@Scope("prototype")
@RequestMapping("/module")
public class ModuleAction extends ExtJSSimpleAction<Module> {
        @Resource(name="moduleService")
        private ModuleService moduleService;
        private String node;
        private boolean privilege=false;
        private boolean recursion=false;      
        @Override
        @ResponseBody
        public String query(){
            if(node==null){
                return super.query();
            }
            //手动缓存控制
            String key="node:"+node+"_privilege:"+privilege+"_recursion:"+recursion;
            //如果privilege=ture，所有用户共享一份数据
            if(!privilege){
                key=UserHolder.getCurrentLoginUser().getUsername()+"_"+key;
            }
            String value=ModuleCache.get(key);
            if(value!=null){
                LOG.debug("使用缓存数据，key:"+key+", value:"+value);
                return value;
            }
            
            long start=System.currentTimeMillis();
            Module module=null;
            if(node.contains("-")){
                String[] temp=node.split("-");
                int id=Integer.parseInt(temp[1]);
                module=moduleService.getModule(id);
            }else if(node.trim().startsWith("root")){
                module=moduleService.getRootModule();
            }
            if(module!=null){
                String json="";
                if(privilege){
                    json=moduleService.toJsonForPrivilege(module);
                }else{
                    json=moduleService.toJsonForUser(module,recursion);
                }
                
                LOG.debug("ModuleAction.query() cost time: "+(System.currentTimeMillis()-start)+" 毫秒");
                LOG.debug("设置缓存数据，key:"+key+", value:"+json);
                ModuleCache.put(key, json);
                return json;
            }
            return null;
        }

        public void setRecursion(boolean recursion) {
            this.recursion = recursion;
        }

        public void setPrivilege(boolean privilege) {
            this.privilege = privilege;
        }

        public void setNode(String node) {
            this.node = node;
        }
}