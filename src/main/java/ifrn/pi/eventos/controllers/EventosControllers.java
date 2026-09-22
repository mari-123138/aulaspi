package ifrn.pi.eventos.controllers;

import ifrn.pi.eventos.models.Convidado;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import ifrn.pi.eventos.models.Evento;
import ifrn.pi.eventos.repositories.ConvidadoRepository;
import ifrn.pi.eventos.repositories.EventoRepository;

@Controller
@RequestMapping("/eventos")
public class EventosControllers {

	@Autowired
	private EventoRepository er;
	@Autowired
	private ConvidadoRepository cr;

	@RequestMapping("/form")
	public String form(Evento evento) {
		return "eventos/formEvento";
	}

	@GetMapping
	public ModelAndView listar() {
		List<Evento> eventos = er.findAll();
		ModelAndView mv = new ModelAndView("eventos/lista");
		mv.addObject("eventos", eventos);
		return mv;
	}

	@PostMapping
	public String salvar(Evento evento) {
		System.out.println(evento);
		er.save(evento);
		return "redirect:/eventos";
	}

	@GetMapping("/{id}")
	public ModelAndView detalhar(@PathVariable Long id, Convidado convidado) {
		ModelAndView md = new ModelAndView();
		Optional<Evento> opt = er.findById(id);
		
		if (opt.isEmpty()) {
			md.setViewName("redirect:/eventos");
			return md;
		}

		md.setViewName("eventos/detalhes");
		Evento evento = opt.get();
		md.addObject("evento", evento);
		
		List<Convidado> convidados = cr.findByEvento(evento);
		md.addObject("convidados", convidados);

		return md;
	}

	@PostMapping("/{idEvento}")
	public String salvarConvidados(@PathVariable Long idEvento, Convidado convidado) {
		System.out.println("Id do evento: " + idEvento);
		System.out.println(convidado);
		
		Optional<Evento> opt = er.findById(idEvento);
		if (opt.isEmpty()) {
			return "redirect:/eventos";
		}
		
		Evento evento = opt.get();
		convidado.setEvento(evento);
		
		cr.save(convidado);
		
		return "redirect:/eventos/{idEvento}";
	}
	
	@GetMapping("/{id}/selecionar")
	public ModelAndView selecionarEvento(@PathVariable Long id) {
		ModelAndView md = new ModelAndView();
		Optional<Evento> opt = er.findById(id);
		if(opt.isEmpty()) {
			md.setViewName("redirect:/eventos");
			return md;
		
		}
		
		Evento evento = opt.get();
		md.setViewName("eventos/formEvento");
		md.addObject("evento", evento);
		
		return md;
	}
	@GetMapping("/{idEvento}/convidado/{idConvidado}/selecionar")
	public ModelAndView selecionarConvidado(@PathVariable Long idEvento, @PathVariable Long idConvidado) {
		ModelAndView md = new ModelAndView();
		
		Optional<Evento> optEvento = er.findById(idEvento);
		Optional<Convidado> optConvidado = cr.findById(idConvidado);
		
		if(optEvento.isEmpty() || optConvidado.isEmpty()) {
			md.setViewName("redirect:/eventos");
			return md;
		}
		
		Evento evento = optEvento.get();
		Convidado convidado = optConvidado.get();
		
		if(evento.getId() != convidado.getEvento().getId()) {
			md.setViewName("redirect:/eventos");
			return md;
		}
		md.setViewName("eventos/detalhes");
		md.addObject("convidado", convidado);
		md.addObject("evento", evento);
		md.addObject("convidados", cr.findByEvento(evento));
		
		return md;
	}

	@GetMapping("/{id}/remover")
	public String apagarEvento(@PathVariable Long id) {
		Optional<Evento> opt = er.findById(id);
		
		if (!opt.isEmpty()) {
			Evento evento = opt.get();
			
			// 1. Busca todos os convidados vinculados ao evento
			List<Convidado> convidados = cr.findByEvento(evento);
			
			// 2. Apaga primeiro todos os convidados para evitar o erro de chave estrangeira (Foreign Key)
			cr.deleteAll(convidados);
			
			// 3. Apaga o evento
			er.delete(evento);
		}
		
		return "redirect:/eventos";
	}

	// NOVO MÉTODO: Para remover um convidado específico da lista do evento
	@GetMapping("/{idEvento}/convidados/{idConvidado}/remover")
	public String apagarConvidado(@PathVariable Long idEvento, @PathVariable Long idConvidado) {
		Optional<Convidado> opt = cr.findById(idConvidado);
		
		if (!opt.isEmpty()) {
			cr.delete(opt.get());
		}
		
		return "redirect:/eventos/" + idEvento;
	}
}